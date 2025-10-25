package vw.viwath.oauth.config

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer

@Configuration
class AccessLogFilter{
    @Bean
    fun nettyServerCustomizer(): WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
        return WebServerFactoryCustomizer { factory ->
            factory.addServerCustomizers({
                    httpServer: HttpServer ->
                httpServer.accessLog(true)
            })
        }
    }
}