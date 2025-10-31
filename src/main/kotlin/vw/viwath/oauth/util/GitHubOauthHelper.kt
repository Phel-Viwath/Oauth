package vw.viwath.oauth.util

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import vw.viwath.oauth.model.GitHubUserInfo
import vw.viwath.oauth.service.implementation.AuthServiceImp

private val logger = LoggerFactory.getLogger(AuthServiceImp::class.java)

suspend fun fetchGitHubUserInfo(
    webClient: WebClient,
    accessToken: String
): GitHubUserInfo? {
    return try {
        webClient.get()
            .uri("https://api.github.com/user")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .awaitBody<GitHubUserInfo>()
    } catch (e: Exception) {
        logger.error("Failed to fetch GitHub user info: ${e.message}")
        null
    }
}

suspend fun fetchPrimaryGitHubEmail(
    webClient: WebClient,
    accessToken: String
): String? {
    return try {
        val emails = webClient.get()
            .uri("https://api.github.com/user/emails")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .awaitBody<List<GitHubEmail>>()

        emails.find { it.primary && it.verified }?.email
    } catch (e: Exception) {
        logger.error("Failed to fetch GitHub emails: ${e.message}")
        null
    }
}