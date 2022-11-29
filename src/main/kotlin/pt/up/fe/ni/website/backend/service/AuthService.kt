package pt.up.fe.ni.website.backend.service

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.up.fe.ni.website.backend.config.AuthConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

@Service
class AuthService(
    val accountService: AccountService,
    val authConfigProperties: AuthConfigProperties,
    val jwtEncoder: JwtEncoder,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticate(email: String, password: String): Authentication {
        val account = accountService.getAccountByEmail(email)
        if (!passwordEncoder.matches(password, account.password)) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid credentials")
        }
        val authorities = listOf("BOARD", "MEMBER").stream() // TODO: get roles from account
            .map { role -> SimpleGrantedAuthority("ROLE_$role") }
            .collect(Collectors.toList())
        return UsernamePasswordAuthenticationToken(email, password, authorities)
    }

    fun generateAccessToken(authentication: Authentication): String {
        val scope = authentication
            .authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "))
        val currentInstant = Instant.now()
        val claims = JwtClaimsSet
            .builder()
            .issuer("self")
            .issuedAt(currentInstant)
            .expiresAt(currentInstant.plus(Duration.of(authConfigProperties.jwtAccessExpirationMinutes, ChronoUnit.MINUTES)))
            .subject(authentication.name)
            .claim("scope", scope)
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    fun getAuthenticatedAccount(): Account {
        val authentication = SecurityContextHolder.getContext().authentication
        return accountService.getAccountByEmail(authentication.name)
    }
}
