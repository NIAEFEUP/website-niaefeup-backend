package pt.up.fe.ni.website.backend.service

import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.stream.Collectors
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.config.auth.AuthConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.service.activity.ActivityService

@Service
class AuthService(
    val accountService: AccountService,
    val activityService: ActivityService,
    val authConfigProperties: AuthConfigProperties,
    val jwtEncoder: JwtEncoder,
    val jwtDecoder: JwtDecoder,
    private val passwordEncoder: PasswordEncoder,
    val repository: ActivityRepository<Project>
) {
    fun hasPermission(permission: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.authorities.any {
            it.toString() == permission
        }
    }

    fun hasActivityPermission(activityId: Long, permission: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication

        val activity = activityService.getActivityById(activityId)
        val name = activity.title.filter { it.isLetterOrDigit() }.uppercase(Locale.getDefault())

        return authentication.authorities.any { it ->
            val payload = it.toString().split(":")
            payload.size == 2 && payload[0] == name && payload[1].split("-").any { p -> p == permission }
        }
    }

    fun authenticate(email: String, password: String): Account {
        val account = accountService.getAccountByEmail(email)
        if (!passwordEncoder.matches(password, account.password)) {
            throw InvalidBearerTokenException(ErrorMessages.invalidCredentials)
        }
        val authentication = UsernamePasswordAuthenticationToken(email, password, generateAuthorities(account))
        SecurityContextHolder.getContext().authentication = authentication
        return account
    }

    fun generateAccessToken(account: Account): String {
        return generateToken(account, Duration.ofMinutes(authConfigProperties.jwtAccessExpirationMinutes))
    }

    fun generateRefreshToken(account: Account): String {
        return generateToken(account, Duration.ofDays(authConfigProperties.jwtRefreshExpirationDays), true)
    }

    fun refreshAccessToken(refreshToken: String): String {
        val jwt = try {
            jwtDecoder.decode(refreshToken)
        } catch (e: Exception) {
            throw InvalidBearerTokenException(ErrorMessages.invalidRefreshToken)
        }
        if (jwt.expiresAt?.isBefore(Instant.now()) != false) {
            throw InvalidBearerTokenException(ErrorMessages.expiredRefreshToken)
        }
        val account = accountService.getAccountByEmail(jwt.subject)
        return generateAccessToken(account)
    }

    fun getAuthenticatedAccount(): Account {
        val authentication = SecurityContextHolder.getContext().authentication
        return accountService.getAccountByEmail(authentication.name)
    }

    private fun generateToken(account: Account, expiration: Duration, isRefresh: Boolean = false): String {
        val roles = if (isRefresh) emptyList() else generateAuthorities(account)
        val scope = roles
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "))
        val currentInstant = Instant.now()
        val claims = JwtClaimsSet
            .builder()
            .issuer("self")
            .issuedAt(currentInstant)
            .expiresAt(currentInstant.plus(expiration))
            .subject(account.email)
            .claim("scope", scope).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    private fun generateAuthorities(account: Account): List<GrantedAuthority> {
        return account.roles.map {
            it.toString().split(" ")
        }
            .flatten()
            .distinct()
            .map {
                SimpleGrantedAuthority(it)
            }
    }
}
