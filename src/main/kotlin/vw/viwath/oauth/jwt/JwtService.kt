package vw.viwath.oauth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import vw.viwath.oauth.common.Token
import vw.viwath.oauth.config.MyKey
import vw.viwath.oauth.model.JwtProperties
import vw.viwath.oauth.model.User
import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class JwtService(
    private val props: JwtProperties,
    private val myKey: MyKey
) {
    val key = myKey.jwtSecret.toByteArray()
    private val secreteKey: Key = Keys.hmacShaKeyFor(key)

//    private val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
//    private val publicKey = keyPair.public
//    private val privateKey = keyPair.private

    fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.accessTokenExpirationSec)
        return Jwts.builder()
            .setSubject(user.userId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .claim("email", user.email)
            .claim("provider", user.provider.name)
            .signWith(secreteKey)
            .compact()
    }

    fun generateRefreshToken(): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.refreshTokenExpirationSec)
        return Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(secreteKey)
            .compact()
    }

    fun validateToken(token: Token, userId: String): Boolean{
        val tokenUserId = getUserIdFromToken(token)
        return tokenUserId == userId && !isTokenExpired(token)
    }

    fun getUserIdFromToken(token: Token): String{
        return getClaimFromToken(token, Claims::getSubject)
    }

    fun getExpirationDate(token: String): Date {
        return getClaimFromToken(token, Claims::getExpiration)
    }

    private fun <T> getClaimFromToken(token: Token, claimResolver: (Claims) -> T): T{
        val claims = getAllClaimFromToken(token)
        return claimResolver(claims)
    }

    private fun getAllClaimFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secreteKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean{
        val expiration = getExpirationDate(token)
        return expiration.before(Date())
    }

    fun isRefreshToken(token: String): Boolean{
        val claim = getAllClaimFromToken(token)
        val issueAt = claim.issuedAt.toInstant()
        val expiration = claim.expiration.toInstant()
        val duration = ChronoUnit.MILLIS.between(issueAt, expiration)
        return duration == 86400000L
    }

}