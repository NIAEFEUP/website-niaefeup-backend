package pt.up.fe.ni.website.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
