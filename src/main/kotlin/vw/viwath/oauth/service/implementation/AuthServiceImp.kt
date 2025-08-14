package vw.viwath.oauth.service.implementation

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import vw.viwath.oauth.common.Email
import vw.viwath.oauth.common.ProviderId
import vw.viwath.oauth.model.AuthProvider
import vw.viwath.oauth.model.AuthResponse
import vw.viwath.oauth.model.LoginRequest
import vw.viwath.oauth.model.RegisterRequest
import vw.viwath.oauth.model.User
import vw.viwath.oauth.model.UserDto
import vw.viwath.oauth.model.toUserDto
import vw.viwath.oauth.repository.AuthRepository
import vw.viwath.oauth.service.AuthService
import vw.viwath.oauth.jwt.JwtService
import java.time.Instant
import java.util.UUID

@Component
class AuthServiceImp(
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) : AuthService {
    override suspend fun register(request: RegisterRequest): UserDto {
        val existingUser = authRepository.findUserByEmail(request.email)
        require(existingUser != null){
            "User already exists with email"
        }
        val hash = passwordEncoder.encode(request.password)
        val user = User(
            userId = UUID.randomUUID().toString(),
            email = request.email,
            passwordHash = hash,
            provider = AuthProvider.LOCAL,
            createAt = Instant.now(),
            updateAt = Instant.now()
        )

        val savedUser = authRepository.save(user)
        return savedUser.toUserDto()
    }

    override suspend fun authenticate(request: LoginRequest): AuthResponse {
        val user = authRepository.findUserByEmail(request.email)
            ?: throw IllegalStateException("Invalid credential.")

        require(user.provider != AuthProvider.LOCAL){
            throw IllegalStateException("User registered via OAuth. Use OAuth login")
        }

        val isPasswordMatch = passwordEncoder.matches(request.password, user.passwordHash)
        if (!isPasswordMatch)
            throw IllegalStateException("Invalid credential.")

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken()

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override suspend fun processOAuthUser(email: Email?, providerId: ProviderId, provider: AuthProvider): AuthResponse {
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
                        userId = UUID.randomUUID().toString(),
                        email = email,
                        passwordHash = null,
                        provider = provider,
                        providerId = providerId,
                    )
                    authRepository.save(newUser)
                }
            }
            else -> throw IllegalArgumentException("OAuth provider didn't return email")
        }

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken()

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override suspend fun refresh(token: String): AuthResponse {
        TODO("Not yet implemented")
    }
}