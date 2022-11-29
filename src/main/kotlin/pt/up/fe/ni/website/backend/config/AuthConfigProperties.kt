package pt.up.fe.ni.website.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConstructorBinding
@ConfigurationProperties(prefix = "auth")
data class AuthConfigProperties(
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey,
    val jwtAccessExpirationMinutes: Long,
    val jwtRefreshExpirationDays: Long
)
