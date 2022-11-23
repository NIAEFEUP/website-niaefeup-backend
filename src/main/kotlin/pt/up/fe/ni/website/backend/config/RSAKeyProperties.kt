package pt.up.fe.ni.website.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConstructorBinding
@ConfigurationProperties(prefix = "rsa")
data class RSAKeyProperties(
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey
)
