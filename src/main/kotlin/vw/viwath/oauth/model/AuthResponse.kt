package vw.viwath.oauth.model

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val errMessage: String? = null,
    val isSuccess: Boolean = false
)