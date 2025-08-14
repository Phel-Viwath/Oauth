package vw.viwath.oauth.model

import vw.viwath.oauth.common.Email

data class UserDto(
    val userId: String,
    val email: Email,
    val provider: AuthProvider = AuthProvider.LOCAL
)

fun User.toUserDto(): UserDto = UserDto(
    userId = this.userId,
    email = this.email,
    provider = this.provider
)