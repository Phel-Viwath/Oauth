package vw.viwath.oauth.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import vw.viwath.oauth.model.AuthProvider
import vw.viwath.oauth.model.User

@Repository
interface AuthRepository: CoroutineCrudRepository<User, String> {
    suspend fun findUserByUserId(userId: String): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun findUserByProviderId(providerId: String): User?
    fun getAllUserByProvider(authProvider: AuthProvider): Flow<User>
}