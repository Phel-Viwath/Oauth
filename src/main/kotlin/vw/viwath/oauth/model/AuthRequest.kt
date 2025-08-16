package vw.viwath.oauth.model

import org.springframework.lang.NonNull

data class AuthRequest(
    @NonNull val email: String,
    @NonNull val password: String
)