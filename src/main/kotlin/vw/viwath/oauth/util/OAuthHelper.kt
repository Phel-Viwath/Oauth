package vw.viwath.oauth.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.service.OAuth2AuthenticationSuccessHandler
import kotlin.collections.find

internal val logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean
)

fun writeJsonResponse(
    objectMapper: ObjectMapper,
    response: ServerHttpResponse,
    apiResponse: ApiResponse<AuthResponse>,
    httpStatus: HttpStatus
) {
    response.statusCode = httpStatus
    response.headers.contentType = MediaType.APPLICATION_JSON

    val jsonResponse = objectMapper.writeValueAsString(apiResponse)
    val buffer = response.bufferFactory().wrap(jsonResponse.toByteArray())

    response.writeWith(Mono.just(buffer)).subscribe()
}

fun extractGoogleUserInfo(attrs: Map<String, Any>): Triple<String?, String?, String?> {
    val email = attrs["email"] as? String
    val name = attrs["name"] as? String
    val providerId = attrs["sub"] as? String // Google's unique user ID

    return Triple(email, name, providerId)
}

suspend fun extractGitHubUserInfo(
    authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    authentication: Authentication,
    webClient: WebClient,
    attrs: Map<String, Any>
): Triple<String?, String?, String?> {
    // GitHub might not provide email in the main response
    val email = attrs["email"] as? String
    val name = attrs["name"] as? String
    val providerId = attrs["id"]?.toString() // GitHub user ID

    // If no email, you might need to fetch it from GitHub API
    // For now, we'll require email or fail
    val finalEmail = email ?: run {
        fetchPrimaryGitHubEmail(
            authorizedClientService,
            authentication,
            webClient
        ).awaitFirstOrNull()
    }

    return Triple(finalEmail, name, providerId)
}

private fun fetchPrimaryGitHubEmail(
    authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    authentication: Authentication,
    webClient: WebClient
): Mono<String?> {
    return mono {
        try {
            val client = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
                (authentication as OAuth2AuthenticationToken).authorizedClientRegistrationId,
                authentication.name
            ).awaitFirstOrNull() // Use awaitFirstOrNull from kotlinx-coroutines-reactor

            val accessToken = client?.accessToken?.tokenValue ?: run {
                logger.warn("Could not get access token for GitHub email fetch.")
                return@mono null
            }

            // Call the GitHub API
            val emails = webClient.get()
                .uri("/user/emails")
                .headers { it.setBearerAuth(accessToken) }
                .retrieve()
                // Deserialize into a List<GitHubEmail>
                .bodyToMono(object : ParameterizedTypeReference<List<GitHubEmail>>() {})
                .awaitFirstOrNull() // Get the list

            // Find the primary, verified email
            emails?.find { it.primary && it.verified }?.email
        } catch (e: Exception) {
            logger.error("Failed to fetch GitHub emails: ${e.message}", e)
            null
        }
    }
}