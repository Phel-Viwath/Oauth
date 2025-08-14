package vw.viwath.oauth.model

import java.time.Instant

data class User(
    val userId: String,
    val email: String,
    val passwordHash: String? = null,
    val provider: AuthProvider = AuthProvider.LOCAL,
    val providerId: String? = null,
    val createAt: Instant = Instant.now(),
    val updateAt: Instant = Instant.now()
)