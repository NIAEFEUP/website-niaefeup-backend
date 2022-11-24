package pt.up.fe.ni.website.backend.service

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.Period
import java.util.stream.Collectors

@Service
class AuthService(
    val accountService: AccountService,
    val jwtEncoder: JwtEncoder,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticate(authentication: Authentication): String {
        if (!okCredentials(authentication)) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid credentials")
        }
        return generateToken(authentication)
    }

    private fun okCredentials(authentication: Authentication): Boolean {
        val account = accountService.getAccountByEmail(authentication.name)
        return passwordEncoder.matches(authentication.credentials.toString(), account.password)
    }

    private fun generateToken(authentication: Authentication): String {
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
            .expiresAt(currentInstant.plus(Period.ofDays(1)))
            .subject(authentication.name)
            .claim("scope", scope)
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}
