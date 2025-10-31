package vw.viwath.oauth.model

data class GitHubUserInfo(
    val id: Long,
    val login: String,
    val email: String?,
    val name: String?
)