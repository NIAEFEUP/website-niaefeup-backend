package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository

@Component
class ActivitySeeder(
    @Autowired val eventSeeder: EventSeeder,
    @Autowired val projectSeeder: ProjectSeeder,
    @Autowired val accountRepository: AccountRepository,
    @Autowired val perActivityRoleRepository: PerActivityRoleRepository
) : AbstractSeeder<ActivityRepository<Event>, Event, Long>(), Logging {

    val accounts = accountRepository.findAll().toList() // Fetch all accounts
    val perActivityRoles = perActivityRoleRepository.findAll().toList() // Fetch all roles

    override fun createObjects() {
        // Delegate seeding to the EventSeeder and ProjectSeeder
        logger.info("Starting Activity seeding process")

        // Seed Events
        eventSeeder.createObjects()

        // Seed Projects
        projectSeeder.createObjects()

        logger.info("Finished seeding activities (events and projects)")
    }
}
