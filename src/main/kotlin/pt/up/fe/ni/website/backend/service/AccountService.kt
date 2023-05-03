package pt.up.fe.ni.website.backend.service

import java.time.Instant
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.auth.ChangePasswordDto
import pt.up.fe.ni.website.backend.dto.auth.PassRecoveryDto
import pt.up.fe.ni.website.backend.dto.entity.account.CreateAccountDto
import pt.up.fe.ni.website.backend.dto.entity.account.UpdateAccountDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.service.upload.FileUploader
import pt.up.fe.ni.website.backend.utils.extensions.filenameExtension

@Service
class AccountService(
    private val repository: AccountRepository,
    private val encoder: PasswordEncoder,
    private val jwtDecoder: JwtDecoder,
    private val fileUploader: FileUploader
) {
    fun getAllAccounts(): List<Account> = repository.findAll().toList()

    fun createAccount(dto: CreateAccountDto): Account {
        repository.findByEmail(dto.email)?.let {
            throw IllegalArgumentException(ErrorMessages.emailAlreadyExists)
        }

        val account = dto.create()
        account.password = encoder.encode(dto.password)

        dto.photoFile?.let {
            val fileName = photoFilename(dto.email, it)
            account.photo = fileUploader.uploadImage("profile", fileName, it.bytes)
        }

        return repository.save(account)
    }

    private fun photoFilename(email: String, photoFile: MultipartFile): String =
        "$email-${UUID.randomUUID()}.${photoFile.filenameExtension()}"

    fun getAccountById(id: Long): Account = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.accountNotFound(id))

    fun doesAccountExist(id: Long): Boolean = repository.findByIdOrNull(id) != null

    fun updateAccountById(id: Long, dto: UpdateAccountDto): Account {
        val account = getAccountById(id)

        repository.findByEmail(dto.email)?.let {
            throw IllegalArgumentException(ErrorMessages.emailAlreadyExists)
        }

        dto.photoFile?.let {
            val fileName = photoFilename(dto.email, it)
            account.photo = fileUploader.uploadImage("profile", fileName, it.bytes)
        }

        val newAccount = dto.update(account)

        return repository.save(newAccount)
    }

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

    fun changePassword(id: Long, dto: ChangePasswordDto) {
        val account = getAccountById(id)
        if (!encoder.matches(dto.oldPassword, account.password)) {
            throw IllegalArgumentException(ErrorMessages.invalidCredentials)
        }
        account.password = encoder.encode(dto.newPassword)
        repository.save(account)
    }

    fun deleteAccountById(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException(ErrorMessages.accountNotFound(id))
        }
        repository.deleteById(id)
    }
}
