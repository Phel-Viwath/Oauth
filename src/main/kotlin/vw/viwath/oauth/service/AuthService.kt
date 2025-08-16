package vw.viwath.oauth.service

import org.springframework.stereotype.Service
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.Email
import vw.viwath.oauth.common.ProviderId
import vw.viwath.oauth.common.Token
import vw.viwath.oauth.model.AuthProvider
import vw.viwath.oauth.model.AuthRequest
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.UserDto

@Service
interface AuthService {
    suspend fun register(request: AuthRequest): ApiResponse<UserDto>
    suspend fun authenticate(request: AuthRequest): ApiResponse<AuthResponse>
    suspend fun processOAuthUser(email: Email?, providerId: ProviderId, provider: AuthProvider): ApiResponse<AuthResponse>
    suspend fun refresh(token: Token): ApiResponse<AuthResponse>

    suspend fun getAllUser(): ApiResponse<List<UserDto>>
}