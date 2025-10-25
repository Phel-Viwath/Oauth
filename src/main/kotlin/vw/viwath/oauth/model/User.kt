package vw.viwath.oauth.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    @Column("userId")
    val userId: String? = null,

    @Column("email")
    val email: String,

    @Column("password")
    val password: String? = null,
    
    @Column("authProvider")
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column("providerId")
    val providerId: String? = null,

    @Column("created_at")
    val createAt: Instant = Instant.now(),

    @Column("updated_at")
    val updateAt: Instant = Instant.now()
)