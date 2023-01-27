package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.AccountDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.repository.AccountRepository

@Service
class AccountService(private val repository: AccountRepository, private val encoder: PasswordEncoder) {
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

    fun checkAccountId(id: Long): Boolean = repository.findByIdOrNull(id) != null

    fun getAccountByEmail(email: String): Account = repository.findByEmail(email)
        ?: throw NoSuchElementException(ErrorMessages.emailNotFound(email))
}
