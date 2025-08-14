package vw.viwath.oauth.jwt

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationWebFilter(
    private val jwtUtil: JwtService
): WebFilter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val token = extractToken(exchange)

        return if (token != null){
            mono {
                try {
                    val userId = jwtUtil.getUserIdFromToken(token)
                    if (jwtUtil.validateToken(token, userId)){
                        val authToken = JwtAuthenticationToken(userId, token)
                        exchange to authToken
                    }else{
                        exchange to null
                    }
                }catch (e: Exception){
                    logger.error(e.message)
                    exchange to null
                }
            }.flatMap { (exc, auth) ->
                if (auth != null){
                    chain.filter(exc)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                }else{
                    chain.filter(exc)
                }
            }
        }else{
            chain.filter(exchange)
        }
    }

    private fun extractToken(
        exchange: ServerWebExchange
    ): String?{
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (authHeader != null && authHeader.startsWith("Bearer "))
            authHeader.substring(7)
        else null
    }
}