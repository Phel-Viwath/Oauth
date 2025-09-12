package vw.viwath.oauth.model

import vw.viwath.oauth.common.ProviderId

data class OAuthUser(
    val email: String? = null,
    val name: String? = null,
    val provider: AuthProvider,
    val providerId: ProviderId
)