package pt.up.fe.ni.website.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import pt.up.fe.ni.website.backend.config.AuthConfigProperties

@SpringBootApplication
@EnableConfigurationProperties(AuthConfigProperties::class)
@EnableJpaAuditing
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
