package pt.up.fe.ni.website.backend.config.auth

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth")
data class AuthConfigProperties(
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey,
    val jwtAccessExpirationMinutes: Long,
    val jwtRefreshExpirationDays: Long
)
