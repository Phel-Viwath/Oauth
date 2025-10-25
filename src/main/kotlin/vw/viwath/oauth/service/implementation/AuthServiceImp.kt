package vw.viwath.oauth.service.implementation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import vw.viwath.oauth.common.ApiResponse
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
            val user = authRepository.findUserByEmail(request.email)
                ?: return ApiResponse.notFound("Invalid credential.")

            if(user.provider != AuthProvider.LOCAL){
                return ApiResponse.badRequest("User registered via OAuth. Use OAuth login")
            }

            val isPasswordMatch = passwordEncoder.matches(request.password, user.password)
            if (!isPasswordMatch)
                return ApiResponse.badRequest("Invalid credential.")

            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken(user)

            val authResponse = AuthResponse(accessToken, refreshToken)
            ApiResponse.success(authResponse)
        }catch (e: Exception){
            logger.error(e.message)
            ApiResponse.internalServerError("Fail to authenticate: ${e.message}")
        }
    }

    override suspend fun processOAuthUser(oAuthUser: OAuthUser): ApiResponse<AuthResponse> {
        val email = oAuthUser.email
        val provider = oAuthUser.provider
        val providerId = oAuthUser.providerId
        return try {
            val existingUserByProvider = authRepository.findUserByProviderId(providerId)
            val user = when {
                existingUserByProvider != null -> {
                    logger.debug("Found existing user by providerId: ${existingUserByProvider.userId}")
                    existingUserByProvider
                }
                email != null -> {
                    val userByEmail = authRepository.findUserByEmail(email)
                    if(userByEmail != null){
                        if(userByEmail.providerId == null){
                            logger.debug("Linking OAuth provider to existing user: ${userByEmail.userId}")
                            val updated = userByEmail.copy(
                                providerId = providerId,
                                provider = provider,
                                updateAt = Instant.now()
                            )
                            authRepository.save(updated)
                        }
                        else if (userByEmail.providerId != providerId)
                            // Email exists but linked to a different OAuth account
                            return ApiResponse.conflict(
                                "An account with this email already exists with a different ${userByEmail.provider} account"
                            )
                        else userByEmail
                    }
                    else {
                        logger.debug("Creating new OAuth user with email: $email")
                        val newUser = User(
                            email = email,
                            password = null,
                            provider = provider,
                            providerId = providerId,
                            createAt = Instant.now(),
                            updateAt = Instant.now()
                        )
                        authRepository.save(newUser)
                    }
                }
                else -> return ApiResponse.badRequest("OAuth provider didn't return email")
            }

            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken(user)

            val auth = AuthResponse(accessToken, refreshToken)
            ApiResponse.success(
                data = auth,
                message = "Authentication successful"
            )
        }catch (e: Exception){
            logger.error("Failed to process OAuth user: ${e.message}", e)
            ApiResponse.internalServerError("Fail to process auth: ${e.message}")
        }
    }

    override suspend fun refresh(token: String): ApiResponse<AuthResponse> {
       return try {
           val userId = jwtService.getUserIdFromToken(token)
           println("userId : $userId")
           val user = authRepository.findById(userId)
               ?: return ApiResponse.badRequest("Invalid token")
           println("$user")
           if (jwtService.validateToken(token, userId) && jwtService.isRefreshToken(token)){
               val newAccessToken = jwtService.generateAccessToken(user)
               val newRefreshToken = jwtService.generateRefreshToken(user)
               val authResponse = AuthResponse(newAccessToken , newRefreshToken)
               ApiResponse.success(authResponse)
           }else{
               ApiResponse.badRequest("Invalid token.")
           }
       }catch (e: Exception){
           logger.error(e.message)
           ApiResponse.internalServerError("Fail to refresh: ${e.message}")
       }
    }

    override fun getAllUser(): Flow<UserDto> = flow{
        val user = authRepository.getAllUserByProvider(AuthProvider.LOCAL).map { it.toUserDto() }

        user.collect {
            delay(100)
            emit(it)
        }
    }
}