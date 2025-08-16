package vw.viwath.oauth.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.toResponseEntity
import vw.viwath.oauth.model.AuthRequest
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.UserDto
import vw.viwath.oauth.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun register(@RequestBody request: AuthRequest): ResponseEntity<ApiResponse<UserDto>>{
        return authService.register(request).toResponseEntity()
    }

    @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun login(@RequestBody request: AuthRequest): ResponseEntity<ApiResponse<AuthResponse>>{
        return authService.authenticate(request).toResponseEntity()
    }

    @GetMapping("/me")
    suspend fun hello(): ResponseEntity<String> = ResponseEntity.ok("Hello")

    @GetMapping("/user")
    suspend fun getAll(): ResponseEntity<ApiResponse<List<UserDto>>>{
        return authService.getAllUser().toResponseEntity()
    }

}