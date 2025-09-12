package vw.viwath.oauth.config.xss
//
//import org.springframework.core.annotation.Order
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import org.springframework.web.server.WebFilter
//import org.springframework.web.server.WebFilterChain
//import reactor.core.publisher.Mono
//import java.util.regex.Pattern
//
//@Component
//@Order(-1)
//class XSSProtectionWebFilter : WebFilter {
//    override fun filter(
//        exchange: ServerWebExchange,
//        chain: WebFilterChain,
//    ): Mono<Void?> {
//        val method = exchange.request.method
//        if(method.name() == "POST" || method.name() == "PUT" || method.name() == "PATCH"){
//            val decoratedExchange = exchange.mutate()
//                .request(XSSProtectionServerHttpRequestDecorator(exchange.request))
//                .build()
//            return chain.filter(decoratedExchange)
//        }
//
//        val sanitizedExchange = exchange.mutate()
//            .request(HeaderSanitizingServerHttpRequestDecorator(exchange.request))
//            .build()
//        return chain.filter(sanitizedExchange)
//    }
//
//    companion object {
//        // XSS patterns to detect malicious scripts
//        private val XSS_PATTERNS = listOf(
//            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
//            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
//            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
//            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
//            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
//            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
//        )
//
//        fun sanitizeInput(value: String): String {
//            var sanitized = value
//            XSS_PATTERNS.forEach { pattern ->
//                sanitized = pattern.matcher(sanitized).replaceAll("")
//            }
//            return sanitized
//                .replace("<", "&lt;")
//                .replace(">", "&gt;")
//                .replace("\"", "&quot;")
//                .replace("'", "&#x27;")
//                .replace("/", "&#x2F;")
//        }
//    }
//
//}