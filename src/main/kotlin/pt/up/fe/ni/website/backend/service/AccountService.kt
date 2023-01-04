package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.dto.AccountDto
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.util.FileUploader
import java.util.UUID

@Service
class AccountService(private val repository: AccountRepository, private val encoder: PasswordEncoder, private val fileUploader: FileUploader) {
    fun getAllAccounts(): List<Account> = repository.findAll().toList()

    fun createAccount(dto: AccountDto): Account {
        repository.findByEmail(dto.email)?.let {
            throw IllegalArgumentException("email already exists")
        }

        val account = dto.create()
        account.password = encoder.encode(dto.password)

        val fileName: String = UUID.randomUUID().toString()
        account.photo = dto.photoFile?.bytes?.let { fileUploader.upload("profile", "$fileName.png", it) }
        return account
        return repository.save(account)
    }

    fun getAccountById(id: Long): Account = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException("account not found with id $id")

    fun getAccountByEmail(email: String): Account = repository.findByEmail(email)
        ?: throw NoSuchElementException("account not found with email $email")
}
