package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.auth.PassRecoveryDto
import pt.up.fe.ni.website.backend.dto.entity.AccountDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.repository.AccountRepository
import java.time.Instant

@Service
class AccountService(
    private val repository: AccountRepository,
    private val encoder: PasswordEncoder,
    private val jwtDecoder: JwtDecoder
) {
    fun getAllAccounts(): List<Account> = repository.findAll().toList()

    fun createAccount(dto: AccountDto): Account {
        repository.findByEmail(dto.email)?.let {
            throw IllegalArgumentException(ErrorMessages.emailAlreadyExists)
        }

        val account = dto.create()
        account.password = encoder.encode(dto.password)
        return repository.save(account)
    }

    fun getAccountById(id: Long): Account = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.accountNotFound(id))

    fun doesAccountExist(id: Long): Boolean = repository.findByIdOrNull(id) != null

    fun getAccountByEmail(email: String): Account = repository.findByEmail(email)
        ?: throw NoSuchElementException(ErrorMessages.emailNotFound(email))

    fun recoverPassword(recoveryToken: String, dto: PassRecoveryDto): Account {
        val jwt =
            try {
                jwtDecoder.decode(recoveryToken)
            } catch (e: Exception) {
                throw InvalidBearerTokenException(ErrorMessages.invalidRecoveryToken)
            }
        if (jwt.expiresAt?.isBefore(Instant.now()) != false) {
            throw InvalidBearerTokenException(ErrorMessages.expiredRecoveryToken)
        }
        val account = getAccountByEmail(jwt.subject)

        account.password = encoder.encode(dto.password)
        return repository.save(account)
    }
}
