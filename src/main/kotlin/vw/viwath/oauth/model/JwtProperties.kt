package vw.viwath.oauth.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessTokenExpirationSec: Long = 60 * 2,
    var refreshTokenExpirationSec: Long = 60 * 60 * 24 * 30 // 30 days
)