package vw.viwath.oauth.service

import org.springframework.stereotype.Service
import vw.viwath.oauth.common.Email
import vw.viwath.oauth.common.ProviderId
import vw.viwath.oauth.model.AuthProvider
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.LoginRequest
import vw.viwath.oauth.model.RegisterRequest
import vw.viwath.oauth.model.UserDto

@Service
interface AuthService {
    suspend fun register(request: RegisterRequest): UserDto
    suspend fun authenticate(request: LoginRequest): AuthResponse
    suspend fun processOAuthUser(email: Email?, providerId: ProviderId, provider: AuthProvider): AuthResponse
    suspend fun refresh(token: String): AuthResponse
}