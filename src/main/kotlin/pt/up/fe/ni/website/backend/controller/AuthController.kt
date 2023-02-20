package pt.up.fe.ni.website.backend.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @PostMapping("/new")
    fun getNewToken(@RequestBody loginDto: LoginDto): Map<String, String> {
        val account = authService.authenticate(loginDto.email, loginDto.password)
        val accessToken = authService.generateAccessToken(account)
        val refreshToken = authService.generateRefreshToken(account)
        return mapOf("access_token" to accessToken, "refresh_token" to refreshToken)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody tokenDto: TokenDto): Map<String, String> {
        val accessToken = authService.refreshAccessToken(tokenDto.token)
        return mapOf("access_token" to accessToken)
    }

    @PostMapping("/recoverPassword/{id}")
    fun generateRecoveryToken(@PathVariable id: Long): Map<String, String> {
        val recoveryToken = authService.generateRecoveryToken(id)
        // TODO: Change URL Later
        return mapOf("recovery_url" to "localhost:8080/accounts/recoverPassword/$recoveryToken")
    }

    @GetMapping
    @PreAuthorize("hasRole('MEMBER')")
    fun checkAuthentication(): Map<String, String> {
        val account = authService.getAuthenticatedAccount()
        return mapOf("authenticated_user" to account.email)
    }
}
