package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.PassRecoveryDto
import pt.up.fe.ni.website.backend.dto.entity.AccountDto
import pt.up.fe.ni.website.backend.service.AccountService

@RestController
@RequestMapping("/accounts")
class AccountController(private val service: AccountService) {
    @GetMapping
    fun getAllAccounts() = service.getAllAccounts()

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long) = service.getAccountById(id)

    @PostMapping("/new")
    fun createAccount(@RequestBody dto: AccountDto) = service.createAccount(dto)

    @PutMapping("/recoverPassword/{recoveryToken}")
    fun recoverPassword(@RequestBody dto: PassRecoveryDto, @PathVariable recoveryToken: String) =
        service.recoverPassword(recoveryToken, dto)
}
