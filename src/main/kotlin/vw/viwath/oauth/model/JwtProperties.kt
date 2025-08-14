package vw.viwath.oauth.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "replace-me-with-secret-key-very-long",
    var accessTokenExpirationSec: Long = 60 * 15, // 15min
    var refreshTokenExpirationSec: Long = 60 * 60 * 24 * 30 // 30 days
)