package pt.up.fe.ni.website.backend.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.PasswordRecoveryConfirmDto
import pt.up.fe.ni.website.backend.dto.auth.PasswordRecoveryRequestDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @field:Value("\${page.recover-password}")
    private lateinit var recoverPasswordPage: String

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

    @PostMapping("/password/recovery")
    fun generateRecoveryToken(@RequestBody recoveryRequestDto: PasswordRecoveryRequestDto): Map<String, String> {
        val recoveryToken = authService.generateRecoveryToken(recoveryRequestDto.email)
        // TODO: Change to email service
        return mapOf("recovery_url" to "$recoverPasswordPage/$recoveryToken/confirm")
    }

    @PostMapping("/password/recovery/{token}/confirm")
    fun confirmRecoveryToken(
        @Valid @RequestBody
        dto: PasswordRecoveryConfirmDto,
        @PathVariable token: String
    ) = authService.confirmRecoveryToken(token, dto)

    @GetMapping
    @PreAuthorize("hasRole('MEMBER')")
    fun checkAuthentication(): Map<String, Account> {
        val account = authService.getAuthenticatedAccount()
        return mapOf("authenticated_user" to account)
    }
}
