package vw.viwath.oauth.model

import vw.viwath.oauth.common.Token

data class AuthResponse(
    val accessToken: Token,
    val refreshToken: Token
)