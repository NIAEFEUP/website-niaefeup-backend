package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Account

@Repository
interface AccountRepository : CrudRepository<Account, Long> {
    fun findByEmail(email: String): Account?
}
