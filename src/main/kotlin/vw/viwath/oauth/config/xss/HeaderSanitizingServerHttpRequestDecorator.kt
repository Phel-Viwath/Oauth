package vw.viwath.oauth.config.xss
//
//import org.springframework.http.HttpHeaders
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.http.server.reactive.ServerHttpRequestDecorator
//import org.springframework.util.LinkedMultiValueMap
//import org.springframework.util.MultiValueMap
//import vw.viwath.oauth.config.xss.XSSProtectionWebFilter.Companion.sanitizeInput
//
//class HeaderSanitizingServerHttpRequestDecorator(
//    delegate: ServerHttpRequest
//): ServerHttpRequestDecorator(delegate) {
//
//    override fun getHeaders(): HttpHeaders {
//        val sanitizeHeaders = HttpHeaders()
//        super.getHeaders().forEach { (key, value) ->
//            sanitizeHeaders[key] = value.map { sanitizeInput(it) }
//        }
//        return sanitizeHeaders
//    }
//
//    override fun getQueryParams(): MultiValueMap<String?, String?> {
//        val sanitizeParams = LinkedMultiValueMap<String, String>()
//        super.getQueryParams().forEach { (key, value) ->
//            sanitizeParams[key] = value.map { sanitizeInput(it) }
//        }
//        return sanitizeParams
//    }
//}