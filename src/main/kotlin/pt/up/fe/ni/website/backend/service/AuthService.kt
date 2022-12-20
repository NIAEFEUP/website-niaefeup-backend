package pt.up.fe.ni.website.backend.service

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
    fun authenticate(email: String, password: String): Account {
        val account = accountService.getAccountByEmail(email)
        if (!passwordEncoder.matches(password, account.password)) {
            throw InvalidBearerTokenException(ErrorMessages.invalidCredentials())
        }
        val authentication = UsernamePasswordAuthenticationToken(email, password, getAuthorities())
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
        val jwt =
            try {
                jwtDecoder.decode(refreshToken)
            } catch (e: Exception) {
                throw InvalidBearerTokenException(ErrorMessages.invalidRefreshToken())
            }
        if (jwt.expiresAt?.isBefore(Instant.now()) != false) {
            throw InvalidBearerTokenException(ErrorMessages.expiredRefreshToken())
        }
        val account = accountService.getAccountByEmail(jwt.subject)
        return generateAccessToken(account)
    }

    fun getAuthenticatedAccount(): Account {
        val authentication = SecurityContextHolder.getContext().authentication
        return accountService.getAccountByEmail(authentication.name)
    }

    private fun generateToken(account: Account, expiration: Duration, isRefresh: Boolean = false): String {
        val roles = if (isRefresh) emptyList() else getAuthorities() // TODO: Pass account to getAuthorities()
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
            .claim("scope", scope)
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    private fun getAuthorities(): List<GrantedAuthority> {
        return listOf("BOARD", "MEMBER").stream() // TODO: get roles from account
            .map { role -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
    }
}
