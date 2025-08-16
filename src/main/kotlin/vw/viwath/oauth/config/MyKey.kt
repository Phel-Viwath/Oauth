package vw.viwath.oauth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class MyKey(
    @Value("\${jwt.secret}") val jwtSecret: String
)