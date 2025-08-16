package vw.viwath.oauth.service

import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import vw.viwath.oauth.model.AuthProvider

@Component
class OAuth2AuthenticationSuccessHandler(
    @Lazy private val authService: AuthService
) : ServerAuthenticationSuccessHandler{
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication,
    ): Mono<Void> = mono{

        val oauth2User = authentication.principal as OAuth2User
        val attrs = oauth2User.attributes
        val email = attrs["email"] as String?
            ?: attrs["login"] as String?
        val providerId = attrs["sub"]?.toString() ?: attrs["id"]?.toString() ?: attrs["node_id"].toString()

        val registrationId = authentication.details?.let {
            (it as? OAuth2AuthenticationToken)?.authorizedClientRegistrationId
        }  ?: (attrs["iss"]?.toString() ?: "unknown")

        val provider = when(registrationId.lowercase()){
            "google" -> AuthProvider.GOOGLE
            "github" -> AuthProvider.GITHUB
            else -> AuthProvider.LOCAL
        }
        val authResponse = authService.processOAuthUser(email, providerId, provider)

        val resp = webFilterExchange.exchange.response
        resp.headers.add("Content-Type", "application/json")
        val json = """
            {
                "accessToken":"${authResponse.data?.accessToken}"
                ,"refreshToken":"${authResponse.data?.refreshToken}"
            }
        """.trimIndent()
        val buffer = resp.bufferFactory().wrap(json.toByteArray())
        resp.writeWith(Mono.just(buffer)).then().subscribe()
        null // return value for coroutine
    }.then()

}