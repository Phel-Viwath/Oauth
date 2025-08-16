package vw.viwath.oauth.jwt

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JwtAuthenticationToken(
    val userId: String,
    val token: String,
    authorities: Collection<GrantedAuthority> = emptyList()
): AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = token
    override fun getPrincipal(): Any? = userId
}