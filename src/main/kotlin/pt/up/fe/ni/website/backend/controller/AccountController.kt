package pt.up.fe.ni.website.backend.controller

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.auth.ChangePasswordDto
import pt.up.fe.ni.website.backend.dto.auth.PasswordRecoveryDto
import pt.up.fe.ni.website.backend.dto.entity.account.CreateAccountDto
import pt.up.fe.ni.website.backend.dto.entity.account.UpdateAccountDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.utils.validation.ValidImage

@RestController
@RequestMapping("/accounts")
@Validated
class AccountController(private val service: AccountService) {
    @GetMapping
    fun getAllAccounts() = service.getAllAccounts()

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long) = service.getAccountById(id)

    @PutMapping("/recoverPassword/{recoveryToken}")
    fun recoverPassword(
        @Valid @RequestBody
        dto: PasswordRecoveryDto,
        @PathVariable recoveryToken: String
    ) =
        service.recoverPassword(recoveryToken, dto)

    @PostMapping("/changePassword/{id}")
    fun changePassword(
        @Valid @RequestBody
        dto: ChangePasswordDto,
        @PathVariable id: Long
    ): Map<String, String> {
        service.changePassword(id, dto)
        return emptyMap()
    }

    @PutMapping("/{id}")
    fun updateAccountById(
        @PathVariable id: Long,
        @RequestPart dto: UpdateAccountDto,
        @RequestParam
        @ValidImage
        photo: MultipartFile?
    ): Account {
        dto.photoFile = photo
        return service.updateAccountById(id, dto)
    }

    @DeleteMapping("/{id}")
    fun deleteAccountById(@PathVariable id: Long): Map<String, String> {
        service.deleteAccountById(id)
        return emptyMap()
    }

    @PostMapping("/new", consumes = ["multipart/form-data"])
    fun createAccount(
        @RequestPart dto: CreateAccountDto,
        @RequestParam
        @ValidImage
        photo: MultipartFile?
    ): Account {
        dto.photoFile = photo
        return service.createAccount(dto)
    }
}
