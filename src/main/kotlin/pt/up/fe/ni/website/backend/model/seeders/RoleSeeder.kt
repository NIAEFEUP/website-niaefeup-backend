package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository

@Component
class RoleSeeder(
    @Autowired private val roleRepository: RoleRepository,
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val generationRepository: GenerationRepository
) : AbstractSeeder<RoleRepository, Role, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running Role seeder...")

        if (roleRepository.count() > 0) {
            logger.info("Skipping Role seeding as roles already exist.")
            return
        }

        val currentGeneration = generationRepository.findAllSchoolYearOrdered().firstOrNull()

        val roles = listOf(
            Role(
                name = "President",
                isSection = true,
                permissions = Permissions(listOf(Permission.EDIT_ACTIVITY))
            ),
            Role(
                name = "Vice President",
                isSection = true,
                permissions = Permissions(listOf(Permission.EDIT_ACTIVITY))
            ),
            Role(
                name = "Member",
                isSection = false,
                permissions = Permissions(listOf(Permission.VIEW_ACTIVITY))
            )
        )

        roleRepository.saveAll(roles)
        logger.info("Seeded ${roles.size} roles into the system.")
    }
}
