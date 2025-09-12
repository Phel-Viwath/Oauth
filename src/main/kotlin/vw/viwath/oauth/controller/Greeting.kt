package vw.viwath.oauth.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class Greeting {

    @GetMapping
    suspend fun greeting(): ResponseEntity<String> = ResponseEntity.ok().body("Hello! What the fuck are you doin' here?")

}