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
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.config.auth.AuthConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.repository.ActivityRepository

@Service
@Component("authService")
class AuthService(
    val accountService: AccountService,
    val authConfigProperties: AuthConfigProperties,
    val jwtEncoder: JwtEncoder,
    val jwtDecoder: JwtDecoder,
    private val passwordEncoder: PasswordEncoder,
    val repository: ActivityRepository<Project>
) {
    fun hasPermission(permission: Permission): Boolean {
        println("here")
        val authorities = SecurityContextHolder.getContext().authentication.authorities
        for (a in authorities) {
            println(a)
        }
        return false
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
        val scope = roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "))
        val currentInstant = Instant.now()
        val claims =
            JwtClaimsSet.builder().issuer("self").issuedAt(currentInstant).expiresAt(currentInstant.plus(expiration))
                .subject(account.email).claim("scope", scope).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    private fun generateAuthorities(account: Account): List<GrantedAuthority> {
        /*val testRole = Role("MEMBER", Permissions(listOf(Permission.CREATE_ACCOUNT, Permission.CREATE_ACTIVITY)), false)
        val testPerActivityRole = PerActivityRole(
            Permissions(listOf(Permission.CREATE_ACCOUNT, Permission.CREATE_ACTIVITY))
        )
        val activity = Project("Test Activity", "Test Description", mutableListOf(), mutableListOf())
        testPerActivityRole.activity = activity
        repository.save(activity)
        testRole.associatedActivities.add(testPerActivityRole)
        account.roles.add(testRole)
        */

        return account.roles.map {
            it.toString().split(" ")
        }.flatten().distinct().map {
            println(it)
            SimpleGrantedAuthority(it)
        }
    }
}
