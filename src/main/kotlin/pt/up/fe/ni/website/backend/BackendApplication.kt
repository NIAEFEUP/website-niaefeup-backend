package pt.up.fe.ni.website.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import pt.up.fe.ni.website.backend.config.RSAKeyProperties

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyProperties::class)
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
