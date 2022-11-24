package pt.up.fe.ni.website.backend.controller

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import pt.up.fe.ni.website.backend.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @PostMapping("/new")
    fun getNewToken(authentication: Authentication?): Map<String, String> {
        if (authentication == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No credentials were provided")
        }
        val token = authService.authenticate(authentication)
        return mapOf("token" to token)
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun authCheck(): Map<String, String> {
        return mapOf("authenticated" to "true")
    }
}
