package vw.viwath.oauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import vw.viwath.oauth.jwt.JwtAuthenticationWebFilter
import vw.viwath.oauth.service.OAuth2AuthenticationSuccessHandler
import vw.viwath.oauth.jwt.JwtService

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val jwtService: JwtService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun serverSecurityFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain{

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/auth/register", "/api/auth/login").permitAll()
                    .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.authenticationSuccessHandler(oAuth2AuthenticationSuccessHandler)
            }
            .oauth2ResourceServer { oauth2 -> oauth2.jwt {  } }
            .addFilterBefore(JwtAuthenticationWebFilter(jwtService), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}