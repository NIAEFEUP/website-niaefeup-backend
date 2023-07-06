package pt.up.fe.ni.website.backend.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService, val repository: ActivityRepository<Project>) {
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun checkAuthentication(): Map<String, Any> {
        val authentication = SecurityContextHolder.getContext().authentication
        val account = authService.getAuthenticatedAccount()
        return mapOf(
            "authenticated_user" to account,
            "jwt_permissions" to authentication.authorities.map { it.toString() }.toList()
        )
    }

    @PreAuthorize("@authService.hasPermission(#permission.trim().toUpperCase())")
    @GetMapping("/hasPermission/{permission}")
    fun protectedEndpoint(@PathVariable permission: String): Map<String, String> {
        return mapOf("message" to "You have permission to access this endpoint!")
    }

    @PreAuthorize("@authService.hasActivityPermission(#activityId, #permission.trim().toUpperCase())")
    @GetMapping("/hasPermission/{activityId}/{permission}")
    fun protected(@PathVariable activityId: Long, @PathVariable permission: String): Map<String, String> {
        return mapOf("message" to "You have permission to access this endpoint!")
    }
}
