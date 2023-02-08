package pt.up.fe.ni.website.backend.service

import java.util.Collections.emptyMap
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.auth.ChangePasswordDto
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

    fun doesAccountExist(id: Long): Boolean = repository.findByIdOrNull(id) != null

    fun updateAccountById(id: Long, dto: AccountDto): Account {
        val account = getAccountById(id)
        val newAccount = dto.update(account)
        return repository.save(newAccount)
    }

    fun getAccountByEmail(email: String): Account = repository.findByEmail(email)
        ?: throw NoSuchElementException(ErrorMessages.emailNotFound(email))

    fun changePassword(id: Long, dto: ChangePasswordDto) {
        val account = getAccountById(id)
        if (!encoder.matches(dto.oldPassword, account.password)) {
            throw IllegalArgumentException(ErrorMessages.invalidCredentials)
        }
        account.password = encoder.encode(dto.newPassword)
        repository.save(account)
    }

    fun deleteAccountById(id: Long): Map<String, String> {
        if (!repository.existsById(id)) {
            throw NoSuchElementException(ErrorMessages.accountNotFound(id))
        }

        repository.deleteById(id)
        return emptyMap()
    }
}
