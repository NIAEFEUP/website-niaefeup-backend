package pt.up.fe.ni.website.backend.service

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.up.fe.ni.website.backend.config.auth.AuthConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import java.time.Duration
import java.time.Instant
import java.util.stream.Collectors

@Service
class AuthService(
    val accountService: AccountService,
    val authConfigProperties: AuthConfigProperties,
    val jwtEncoder: JwtEncoder,
    val jwtDecoder: JwtDecoder,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticate(email: String, password: String): Authentication {
        val account = accountService.getAccountByEmail(email)
        if (!passwordEncoder.matches(password, account.password)) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid credentials")
        }
        val authorities = listOf("BOARD", "MEMBER").stream() // TODO: get roles from account
            .map { role -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
        return UsernamePasswordAuthenticationToken(email, password, authorities)
    }

    fun generateAccessToken(authentication: Authentication): String {
        return generateToken(authentication, Duration.ofMinutes(authConfigProperties.jwtAccessExpirationMinutes))
    }

    fun generateRefreshToken(authentication: Authentication): String {
        return generateToken(authentication, Duration.ofDays(authConfigProperties.jwtRefreshExpirationDays), true)
    }

    fun refreshToken(refreshToken: String): String {
        val jwt = jwtDecoder.decode(refreshToken)
        if (jwt.expiresAt?.isBefore(Instant.now()) != false) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired")
        }
        val account = accountService.getAccountByEmail(jwt.subject)
        val authentication = authenticate(account.email, account.password)
        return generateAccessToken(authentication)
    }

    fun getAuthenticatedAccount(): Account {
        val authentication = SecurityContextHolder.getContext().authentication
        return accountService.getAccountByEmail(authentication.name)
    }

    private fun generateToken(authentication: Authentication, expiration: Duration, isRefresh: Boolean = false): String {
        val roles = if (isRefresh) emptyList<GrantedAuthority>() else authentication.authorities
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
            .subject(authentication.name)
            .claim("scope", scope)
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}
