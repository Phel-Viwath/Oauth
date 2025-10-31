package vw.viwath.oauth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class MyKey(
    @param:Value("\${jwt.secret}") val jwtSecret: String,
    @param:Value("\${spring.security.oauth2.client.registration.google.client-id}") val googleClientId: String,
)