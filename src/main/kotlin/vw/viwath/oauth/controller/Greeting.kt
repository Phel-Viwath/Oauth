package vw.viwath.oauth.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.toResponseEntity

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class Greeting {

    @GetMapping
    suspend fun greeting(): ResponseEntity<ApiResponse<Map<String, String>>> =
        ApiResponse.success(mapOf("message" to "Hello!"), message = "OK").toResponseEntity()
}