package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.ChangePasswordDto
import pt.up.fe.ni.website.backend.dto.entity.account.CreateAccountDto
import pt.up.fe.ni.website.backend.dto.entity.account.UpdateAccountDto
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.annotations.validation.ValidImage
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.service.AccountService

@RestController
@RequestMapping("/accounts")
@Validated
class AccountController(private val service: AccountService) {
    @GetMapping
    fun getAllAccounts() = service.getAllAccounts()

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long) = service.getAccountById(id)

    @PostMapping("/changePassword/{id}")
    fun changePassword(@PathVariable id: Long, @RequestBody dto: ChangePasswordDto): Map<String, String> {
        service.changePassword(id, dto)
        return emptyMap()
    }

    @PutMapping("/{id}")
    fun updateAccountById(
        @PathVariable id: Long,
        @RequestBody dto: UpdateAccountDto
    ) = service.updateAccountById(id, dto)

    @DeleteMapping("/{id}")
    fun deleteAccountById(@PathVariable id: Long): Map<String, String> {
        service.deleteAccountById(id)
        return emptyMap()
    }
    @PostMapping("/new")
    fun createAccount(@RequestPart account: CreateAccountDto, @RequestPart @ValidImage photo: MultipartFile?): Account {
        account.photoFile = photo
        return service.createAccount(account)
    }
}
