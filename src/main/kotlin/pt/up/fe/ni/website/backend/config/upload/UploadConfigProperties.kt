package pt.up.fe.ni.website.backend.config.upload

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "upload")
class UploadConfigProperties(
    val uploadType: String?,
    val cloudinaryUrl: String?,
    val staticPath: String?
)
