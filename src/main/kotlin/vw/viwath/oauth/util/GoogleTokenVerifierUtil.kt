package vw.viwath.oauth.util

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import vw.viwath.oauth.config.MyKey

@Component
class GoogleTokenVerifierUtil(
    private val myKey: MyKey
) {
    private val logger = LoggerFactory.getLogger(GoogleTokenVerifierUtil::class.java)

    fun verifyGoogleIdToken(id: String): GoogleIdToken.Payload? {
        val verifier = GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ).setAudience(listOf(myKey.googleClientId)).build()

        return try {
            val googleIdTokenId = verifier.verify(id)
            googleIdTokenId?.payload
        }catch (e: Exception){
            logger.error("Failed to verify Google ID token: ${e.message}")
            null
        }
    }
}