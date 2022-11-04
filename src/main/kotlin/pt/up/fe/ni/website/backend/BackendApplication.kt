package pt.up.fe.ni.website.backend

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(info=Info(title="NI Website REST API"))

class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
