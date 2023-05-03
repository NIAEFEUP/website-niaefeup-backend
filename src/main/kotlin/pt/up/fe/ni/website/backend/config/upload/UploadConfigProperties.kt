package pt.up.fe.ni.website.backend.config.upload

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "upload")
class UploadConfigProperties(
    val provider: String?,
    val cloudinaryUrl: String?,
    val cloudinaryBasePath: String?,
    val staticPath: String?,
    val staticServe: String?
)
