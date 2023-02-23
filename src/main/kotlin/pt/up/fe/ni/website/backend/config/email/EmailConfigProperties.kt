package pt.up.fe.ni.website.backend.config.email

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "email")
data class EmailConfigProperties(
    val from: String,
    val fromPersonal: String?
)
