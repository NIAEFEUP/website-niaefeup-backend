package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.dto.AccountDto
import pt.up.fe.ni.website.backend.repository.AccountRepository

@Service
class AccountService(private val repository: AccountRepository) {
    fun getAllAccounts(): List<Account> = repository.findAll().toList()

    fun createAccount(dto: AccountDto): Account {
        repository.findByEmail(dto.email)?.let {
            throw IllegalArgumentException("email already exists")
        }

        val account = dto.create()
        return repository.save(account)
    }

    fun getAccountById(id: Long): Account = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException("account not found with id $id")
}
