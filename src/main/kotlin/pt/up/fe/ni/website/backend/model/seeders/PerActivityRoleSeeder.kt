package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository

@Component
class PerActivityRoleSeeder(
    @Autowired roleRepository: RoleRepository
) : AbstractSeeder<PerActivityRoleRepository, PerActivityRole, Long>(), Logging {

    override fun createObjects() {
    }
}
