package vw.viwath.oauth.model

import vw.viwath.oauth.common.Email
import vw.viwath.oauth.common.Password

data class RegisterRequest(
    val email: Email,
    val password: Password
)