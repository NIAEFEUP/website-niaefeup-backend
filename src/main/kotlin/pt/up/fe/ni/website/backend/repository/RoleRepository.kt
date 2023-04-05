package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Role

@Repository
interface RoleRepository : CrudRepository<Role, Long> {
    fun findByName(name: String): Role?
}
