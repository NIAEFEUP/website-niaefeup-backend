package pt.up.fe.ni.website.backend.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ErrorController : ErrorController {
    val errorKey: String = "error"

    @RequestMapping("/**")
    fun endpointNotFound() = mapOf(errorKey to "invalid endpoint")
}
