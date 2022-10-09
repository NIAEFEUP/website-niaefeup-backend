package pt.up.fe.ni.website.backend.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class ErrorController : ErrorController {
    val errorKey: String = "error"

    @RequestMapping("/**")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidRequest() = mapOf(errorKey to "invalid request")
}
