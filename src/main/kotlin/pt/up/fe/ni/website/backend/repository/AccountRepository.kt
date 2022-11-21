package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import pt.up.fe.ni.website.backend.model.Account

interface AccountRepository : CrudRepository<Account, Long> {
    fun findByEmail(email: String): Account?
}
