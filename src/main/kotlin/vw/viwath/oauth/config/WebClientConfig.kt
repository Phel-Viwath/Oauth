package vw.viwath.oauth.config

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.NettyPipeline.ReadTimeoutHandler
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {

    @Bean
    fun webClient() : WebClient{
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30))
            .doOnDisconnected { con ->
                con.addHandlerLast(ReadTimeoutHandler(30))
                con.addHandlerLast(WriteTimeoutHandler(30))
            }
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}