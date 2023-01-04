package pt.up.fe.ni.website.backend.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.annotations.validation.ValidImage
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.dto.AccountDto
import pt.up.fe.ni.website.backend.service.AccountService

@RestController
@RequestMapping("/accounts")
class AccountController(private val service: AccountService) {
    @GetMapping
    fun getAllAccounts() = service.getAllAccounts()

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long) = service.getAccountById(id)

    @PostMapping("/new")
    @Validated
    fun createAccount(@RequestPart account: AccountDto, @RequestPart @ValidImage photo: MultipartFile?): Account {
        account.photoFile = photo
        return service.createAccount(account)
    }
}
