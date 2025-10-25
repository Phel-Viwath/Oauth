package vw.viwath.oauth.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.model.AuthProvider
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.OAuthUser
import vw.viwath.oauth.util.extractGitHubUserInfo
import vw.viwath.oauth.util.extractGoogleUserInfo
import vw.viwath.oauth.util.writeJsonResponse

@Component
class OAuth2AuthenticationSuccessHandler(
    @param:Lazy private val authService: AuthService,
    private val objectMapper: ObjectMapper,
    private val authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    webClientBuilder: WebClient.Builder
) : ServerAuthenticationSuccessHandler{

    private val logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    // Create a WebClient instance to call the GitHub API
    private val webClient: WebClient = webClientBuilder.baseUrl("https://api.github.com").build()

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication,
    ): Mono<Void> = mono{

        val oauth2User = authentication.principal as OAuth2User
        val attrs = oauth2User.attributes

        val provider = when (authentication) {
            is OAuth2AuthenticationToken -> {
                when (authentication.authorizedClientRegistrationId) {
                    "google" -> AuthProvider.GOOGLE
                    "github" -> AuthProvider.GITHUB
                    else -> AuthProvider.LOCAL
                }
            }
            else -> AuthProvider.LOCAL
        }

        // Extract user info based on the provider
        val (email, name, providerId) = when (provider) {
            AuthProvider.GOOGLE -> extractGoogleUserInfo(attrs)
            AuthProvider.GITHUB -> extractGitHubUserInfo(
                authorizedClientService,
                authentication,
                webClient,
                attrs
            )
            else -> Triple(null, null, null)
        }

        val response = webFilterExchange.exchange.response

        if (email == null || providerId == null) {
            logger.error("Failed to extract user info from OAuth provider: $provider")
            val errorResponse = ApiResponse.badRequest<AuthResponse>(
                "Failed to get user information from provider"
            )
            writeJsonResponse(objectMapper, response, errorResponse, HttpStatus.BAD_REQUEST)
            return@mono null
        }

        logger.debug("OAuth login - Provider: {}, Email: {}", provider, email)


        val oAuthUser = OAuthUser(
            email,
            provider,
            providerId
        )

        try {
            // Process OAuth user and generate our own JWT tokens
            val authResponse = authService.processOAuthUser(oAuthUser)

            // Write JSON response with a consistent format
            val httpStatus = when (authResponse.code) {
                200 -> HttpStatus.OK
                201 -> HttpStatus.CREATED
                400 -> HttpStatus.BAD_REQUEST
                404 -> HttpStatus.NOT_FOUND
                409 -> HttpStatus.CONFLICT
                500 -> HttpStatus.INTERNAL_SERVER_ERROR
                else -> HttpStatus.valueOf(authResponse.code)
            }

            writeJsonResponse(objectMapper, response, authResponse, httpStatus)
        } catch (e: Exception) {
            logger.error("Error processing OAuth user: ${e.message}", e)
            val errorResponse = ApiResponse.internalServerError<AuthResponse>(
                "Failed to process OAuth authentication: ${e.message}"
            )
            writeJsonResponse(objectMapper, response, errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        null
    }.then()

}