package vw.viwath.oauth.config.xss
//
//import com.fasterxml.jackson.databind.JsonNode
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.slf4j.LoggerFactory
//import org.springframework.core.io.buffer.DataBuffer
//import org.springframework.core.io.buffer.DataBufferUtils
//import org.springframework.core.io.buffer.DefaultDataBufferFactory
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.http.server.reactive.ServerHttpRequestDecorator
//import reactor.core.publisher.Flux
//import vw.viwath.oauth.config.xss.XSSProtectionWebFilter.Companion.sanitizeInput
//import java.nio.charset.StandardCharsets
//
//class XSSProtectionServerHttpRequestDecorator(
//    delegate: ServerHttpRequest
//): ServerHttpRequestDecorator(delegate) {
//
//    private val logger = LoggerFactory.getLogger(XSSProtectionServerHttpRequestDecorator::class.java)
//
//    override fun getBody(): Flux<DataBuffer> {
//        return super.getBody()
//            .collectList()
//            .map { dataBuffers ->
//                val content = dataBuffers.joinToString("") { buffer ->
//                    val bytes = ByteArray(buffer.readableByteCount())
//                    buffer.read(bytes)
//                    DataBufferUtils.release(buffer)
//                    String(bytes, StandardCharsets.UTF_8)
//                }
//
//                val sanitizeContent = sanitizeRequestBody(content)
//                val buffers: DataBuffer = DefaultDataBufferFactory
//                    .sharedInstance
//                    .wrap(sanitizeContent.toByteArray(StandardCharsets.UTF_8))
//                buffers
//            }
//            .flux()
//    }
//
//    private fun sanitizeRequestBody(body: String): String{
//        if(body.isEmpty()) return body
//
//        return try {
//            val objectMapper = ObjectMapper()
//            val jsonNode = objectMapper.readTree(body)
//            val sanitizeMode = sanitizeJsonNode(jsonNode, objectMapper)
//            objectMapper.writeValueAsString(sanitizeMode)
//        }catch (e: Exception){
//            logger.error(e.message)
//            sanitizeInput(body)
//        }
//    }
//
//    private fun sanitizeJsonNode(node: JsonNode, objectMapper: ObjectMapper): JsonNode{
//        return when{
//            node.isObject -> {
//                val objectNode = objectMapper.createObjectNode()
//                node.properties().forEach { (k, v) ->
//                    objectNode.set<JsonNode>(sanitizeInput(k), sanitizeJsonNode(v, objectMapper))
//                }
//                objectNode
//            }
//            node.isArray -> {
//                val arrayNode = objectMapper.createArrayNode()
//                node.forEach { arrayNode.add(sanitizeJsonNode(it, objectMapper)) }
//                arrayNode
//            }
//            node.isTextual -> {
//                objectMapper.valueToTree(node.asText())
//            }
//            else -> node
//        }
//    }
//}