package pt.up.fe.ni.website.backend.service

import java.time.Duration
import java.time.Instant
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
import pt.up.fe.ni.website.backend.dto.auth.PasswordRecoveryConfirmDto
import pt.up.fe.ni.website.backend.model.Account

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
            throw InvalidBearerTokenException(ErrorMessages.invalidCredentials)
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
        val jwt = jwtDecoder.decode(refreshToken)
        val account = accountService.getAccountByEmail(jwt.subject)
        return generateAccessToken(account)
    }

    fun generateRecoveryToken(email: String): String? {
        val account = try {
            accountService.getAccountByEmail(email)
        } catch (e: Exception) {
            return null
        }
        return generateToken(
            account,
            Duration.ofMinutes(authConfigProperties.jwtRecoveryExpirationMinutes),
            usePasswordHash = true
        )
    }

    fun confirmRecoveryToken(recoveryToken: String, dto: PasswordRecoveryConfirmDto): Account {
        val jwt = jwtDecoder.decode(recoveryToken)
        val account = accountService.getAccountByEmail(jwt.subject)

        val tokenPasswordHash = jwt.getClaim<String>("passwordHash")
            ?: throw InvalidBearerTokenException(ErrorMessages.invalidToken)

        if (account.password != tokenPasswordHash) {
            throw InvalidBearerTokenException(ErrorMessages.invalidToken)
        }

        account.password = passwordEncoder.encode(dto.password)
        return accountService.updateAccount(account)
    }

    fun getAuthenticatedAccount(): Account {
        val authentication = SecurityContextHolder.getContext().authentication
        return accountService.getAccountByEmail(authentication.name)
    }

    private fun generateToken(
        account: Account,
        expiration: Duration,
        isRefresh: Boolean = false,
        usePasswordHash: Boolean = false
    ): String {
        val roles = if (isRefresh) emptyList() else getAuthorities() // TODO: Pass account to getAuthorities()
        val scope = roles
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "))
        val currentInstant = Instant.now()
        val claimsBuilder = JwtClaimsSet
            .builder()
            .issuer("self")
            .issuedAt(currentInstant)
            .expiresAt(currentInstant.plus(expiration))
            .subject(account.email)
            .claim("scope", scope)

        if (usePasswordHash) {
            claimsBuilder.claim("passwordHash", account.password)
        }

        val claims = claimsBuilder.build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    private fun getAuthorities(): List<GrantedAuthority> {
        return listOf("BOARD", "MEMBER").stream() // TODO: get roles from account
            .map { role -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
    }
}
