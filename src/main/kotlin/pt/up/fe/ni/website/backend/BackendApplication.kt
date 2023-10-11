package pt.up.fe.ni.website.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import pt.up.fe.ni.website.backend.config.auth.AuthConfigProperties
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties
import pt.up.fe.ni.website.backend.config.upload.UploadConfigProperties

@SpringBootApplication
@EnableConfigurationProperties(AuthConfigProperties::class, UploadConfigProperties::class, EmailConfigProperties::class)
@EnableJpaAuditing
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
