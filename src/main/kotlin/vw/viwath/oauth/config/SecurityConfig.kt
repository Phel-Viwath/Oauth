package vw.viwath.oauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import reactor.core.publisher.Mono

import vw.viwath.oauth.jwt.JwtAuthenticationWebFilter
import vw.viwath.oauth.jwt.JwtService
import vw.viwath.oauth.service.OAuth2AuthenticationSuccessHandler
import java.time.Duration
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val jwtService: JwtService,
    private val myKey: MyKey
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val secretKey = SecretKeySpec(myKey.jwtSecret.toByteArray(), "HmacSHA512")
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS512)
            .build()
    }

//    @Bean
//    fun xssWebFilter(): XSSProtectionWebFilter = XSSProtectionWebFilter()

    @Bean
    fun serverSecurityFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain{

        return http
            .headers { header ->
                header
                    .contentTypeOptions{}
                    .frameOptions { it.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY) }
                    .contentSecurityPolicy{ contentSecurityPolicySpec ->
                        contentSecurityPolicySpec
                            .policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none';"
                            )
                    }
                    .hsts { hstsSpec ->
                        hstsSpec
                            .maxAge(Duration.ofDays(365))
                            .includeSubdomains(true)
                    }
                    .referrerPolicy { rpc ->
                        rpc.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
                    .writer { exchange ->
                        val headersMap = exchange.request.headers
                        headersMap.add("X-XSS-Protection", "1; mode=block")
                        headersMap.add("X-Content-Type-Options", "nosniff")
                        headersMap.add("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
                        Mono.empty()
                    }

            }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            //.formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/user",
                        "api/auth/refresh"
                    ).permitAll()
                    .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.authenticationSuccessHandler(oAuth2AuthenticationSuccessHandler)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(jwtDecoder())
                }
            }
//            .addFilterBefore(xssWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterBefore(JwtAuthenticationWebFilter(jwtService), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}