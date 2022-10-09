package pt.up.fe.ni.website.backend.controller

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.service.AuthService

@RestController
class AuthController(val authService: AuthService) {

    @PostMapping("/token")
    fun getToken(authentication: Authentication) = authService.generateToken(authentication)

    // Just a locked endpoint to test the auth... Delete this when authorization is properly handled (permissions)
    @GetMapping("/secret")
    fun authCheck(): Map<String, String> {
        return mapOf("authenticated" to "true")
    }
}
