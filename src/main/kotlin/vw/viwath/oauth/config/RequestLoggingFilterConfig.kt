package vw.viwath.oauth.config

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Component
class RequestLoggingFilterConfig {

    @Suppress("UsePropertyAccessSyntax")
    @Bean
    fun logFilter(): CommonsRequestLoggingFilter{
        val filter = CommonsRequestLoggingFilter()
        filter.apply {
            setIncludeQueryString(true)
            setIncludePayload(true)
            setMaxPayloadLength(10000)
            setIncludeHeaders(false)
            setAfterMessagePrefix("REQUEST DATA: ")
        }
        return filter
    }
}