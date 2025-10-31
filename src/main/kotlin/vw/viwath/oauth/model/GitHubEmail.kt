package vw.viwath.oauth.model

data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean
)