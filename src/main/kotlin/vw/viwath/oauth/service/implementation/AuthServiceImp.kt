package vw.viwath.oauth.service.implementation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import vw.viwath.oauth.common.ApiResponse
import vw.viwath.oauth.common.Email
import vw.viwath.oauth.common.ProviderId
import vw.viwath.oauth.jwt.JwtService
import vw.viwath.oauth.model.*
import vw.viwath.oauth.repository.AuthRepository
import vw.viwath.oauth.service.AuthService
import java.time.Instant

@Component
class AuthServiceImp(
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) : AuthService {

    private val logger = LoggerFactory.getLogger(AuthServiceImp::class.java)

    override suspend fun register(request: AuthRequest): ApiResponse<UserDto> {
        return try {
            if (request.email.isEmpty() || request.password.isEmpty())
                return ApiResponse.badRequest("Field cannot be empty.")

            val existingUser = authRepository.findUserByEmail(request.email)
            if (existingUser != null)
                return ApiResponse.conflict(message = "User already exists with email")

            val hash = passwordEncoder.encode(request.password)
            val user = User(
                email = request.email,
                password = hash,
                provider = AuthProvider.LOCAL,
                createAt = Instant.now(),
                updateAt = Instant.now()
            )
            val savedUser = authRepository.save(user)
            logger.info("$savedUser")
            ApiResponse.created(savedUser.toUserDto())
        }catch (e: Exception){
            logger.error(e.message)
            ApiResponse.internalServerError("Failed to create user: ${e.message}")
        }
    }

    override suspend fun authenticate(request: AuthRequest): ApiResponse<AuthResponse> {
        return try {
            if (request.email.isEmpty() || request.password.isEmpty())
                return ApiResponse.badRequest("Field cannot be empty.")

            val user = authRepository.findUserByEmail(request.email)
                ?: return ApiResponse.notFound("Invalid credential.")

            if(user.provider != AuthProvider.LOCAL){
                return ApiResponse.badRequest("User registered via OAuth. Use OAuth login")
            }

            val isPasswordMatch = passwordEncoder.matches(request.password, user.password)
            if (!isPasswordMatch)
                return ApiResponse.badRequest("Invalid credential.")

            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken()

            val authResponse = AuthResponse(accessToken, refreshToken)
            ApiResponse.success(authResponse)
        }catch (e: Exception){
            ApiResponse.internalServerError("Fail to authenticate: ${e.message}")
        }
    }

    override suspend fun processOAuthUser(
        email: Email?,
        providerId: ProviderId,
        provider: AuthProvider
    ): ApiResponse<AuthResponse> {
        return try {
            val exitingUserByProvider = authRepository.findUserByProviderId(providerId)
            val user = when{
                exitingUserByProvider != null -> exitingUserByProvider
                email != null -> {
                    val userByEmail = authRepository.findUserByEmail(email)
                    if(userByEmail != null){
                        if(userByEmail.providerId == null){
                            val update = userByEmail.copy(providerId = providerId, provider = provider)
                            authRepository.save(update)
                        }
                        else userByEmail
                    }
                    else{
                        val newUser = User(
                            email = email,
                            password = null,
                            provider = provider,
                            providerId = providerId,
                        )
                        authRepository.save(newUser)
                    }
                }
                else -> return ApiResponse.badRequest("OAuth provider didn't return email")
            }

            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken()

            val auth = AuthResponse(accessToken, refreshToken)
            ApiResponse.success(auth)
        }catch (e: Exception){
            ApiResponse.internalServerError("Fail to process auth: ${e.message}")
        }
    }

    override suspend fun refresh(token: String): ApiResponse<AuthResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUser(): ApiResponse<List<UserDto>> {
        return try {
            val user = authRepository.getAllUserByProvider(AuthProvider.LOCAL).map { it.toUserDto() }.toList()
            ApiResponse.success(user)
        }catch (e: Exception){
            ApiResponse.internalServerError("Fail to get user: ${e.message}")
        }
    }
}