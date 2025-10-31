package vw.viwath.oauth.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.Token
import vw.viwath.oauth.model.AuthRequest
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.OAuthUser
import vw.viwath.oauth.model.UserDto

@Service
interface AuthService {
    // In AuthService.kt interface
    suspend fun processGitHubAccessToken(accessToken: String): ApiResponse<AuthResponse>
    suspend fun processGoogleIdToken(idToken: String): ApiResponse<AuthResponse>
    suspend fun register(request: AuthRequest): ApiResponse<UserDto>
    suspend fun authenticate(request: AuthRequest): ApiResponse<AuthResponse>
    suspend fun processOAuthUser(oAuthUser: OAuthUser): ApiResponse<AuthResponse>
    suspend fun refresh(token: Token): ApiResponse<AuthResponse>

    fun getAllUser(): Flow<UserDto>
}