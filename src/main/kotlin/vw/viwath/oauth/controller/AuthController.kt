package vw.viwath.oauth.controller

import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.HtmlUtils
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.Token
import vw.viwath.oauth.common.toResponseEntity
import vw.viwath.oauth.model.AuthRequest
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.UserDto
import vw.viwath.oauth.service.AuthService

@Suppress("")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody request: AuthRequest): ResponseEntity<ApiResponse<UserDto>>{
        val areFieldBlank = request.email.isEmpty() || request.password.isEmpty()
        val isPasswordShort = request.password.length < 8
        return when{
            areFieldBlank -> ApiResponse.badRequest<UserDto>("Field cannot be blank.").toResponseEntity()
            isPasswordShort -> ApiResponse.badRequest<UserDto>("Password is too short.").toResponseEntity()
            else -> authService.register(request).toResponseEntity()
        }
    }

    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<ApiResponse<AuthResponse>>{
        val areFieldBlank = request.email.isEmpty() || request.password.isEmpty()
        val safeEmail = HtmlUtils.htmlEscape(request.email)
        return when{
            areFieldBlank -> ApiResponse.badRequest<AuthResponse>("Field cannot be blank").toResponseEntity()
            else -> {
                val safeRequest = request.copy(email = safeEmail)
                authService.authenticate(safeRequest).toResponseEntity()
            }
        }
    }

    @PostMapping("/refresh")
    suspend fun refresh(
        @RequestParam refreshToken: Token
    ): ResponseEntity<ApiResponse<AuthResponse>>{
        val safeToken = HtmlUtils.htmlEscape(refreshToken)
        return authService.refresh(token = safeToken).toResponseEntity()
    }

    @GetMapping("/me")
    suspend fun hello(): ResponseEntity<String> = ResponseEntity.ok("Hello")

    @GetMapping("/user")
    suspend fun getAll(): Flow<UserDto>{
        return authService.getAllUser()
    }

}