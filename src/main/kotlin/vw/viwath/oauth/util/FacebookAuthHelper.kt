package vw.viwath.oauth.util

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import vw.viwath.oauth.service.implementation.AuthServiceImp

private val logger = LoggerFactory.getLogger(AuthServiceImp::class.java)

suspend fun fetchFacebookUserInfo(
    webClient: WebClient,
    accessToken: String
): FacebookUserInfo? {
    return try {
        webClient.get()
            .uri("https://graph.facebook.com/me?fields=id,name,email&access_token=$accessToken")
            .retrieve()
            .awaitBody<FacebookUserInfo>()
    }catch (e: Exception){
        logger.error("Failed to fetch Facebook user info: ${e.message}")
        null
    }
}

data class FacebookUserInfo(
    val id: String,
    val name: String?,
    val email: String?
)
