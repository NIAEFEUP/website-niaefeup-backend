package pt.up.fe.ni.website.backend.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.service.AuthService

data class LoginDto(
    val email: String,
    val password: String
)

data class TokenDto(
    val token: String
)

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @PostMapping("/new")
    fun getNewToken(@RequestBody loginDto: LoginDto): Map<String, String> {
        val authentication = authService.authenticate(loginDto.email, loginDto.password)
        val accessToken = authService.generateAccessToken(authentication)
        val refreshToken = authService.generateRefreshToken(authentication)
        return mapOf("access_token" to accessToken, "refresh_token" to refreshToken)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody tokenDto: TokenDto): Map<String, String> {
        val accessToken = authService.refreshToken(tokenDto.token)
        return mapOf("access_token" to accessToken)
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun checkAuthentication(): Map<String, String> {
        val account = authService.getAuthenticatedAccount()
        return mapOf("authenticated_user" to account.email)
    }
}
