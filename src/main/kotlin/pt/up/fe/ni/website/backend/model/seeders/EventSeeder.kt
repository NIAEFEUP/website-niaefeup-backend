package pt.up.fe.ni.website.backend.model.seeders

import java.util.Date
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository

@Component
class EventSeeder(
    @Autowired val accountRepository: AccountRepository,
    @Autowired val perActivityRoleRepository: PerActivityRoleRepository
) : AbstractSeeder<EventRepository, Event, Long>(), Logging {

    val accounts = accountRepository.findAll().toList()
    val perActivityRoles = perActivityRoleRepository.findAll().toList()

    override fun createObjects() {
        if (repository.count() == 0L) {
            logger.info("Seeding Event data...")

            val events = listOf(
                Event(
                    title = "Tech Conference 2024",
                    description = "A yearly tech conference bringing together industry experts and enthusiasts.",
                    teamMembers = accounts.subList(0, 3).toMutableList(),
                    associatedRoles = perActivityRoles.subList(0, 2).toMutableList(),
                    slug = "tech-conference-2024",
                    image = "conference_image.png",
                    registerUrl = "https://conference.com/register",
                    dateInterval = DateInterval(
                        startDate = Date.from(faker.date().birthday().toInstant()),
                        endDate = Date.from(faker.date().birthday().toInstant())

                    ),
                    location = "Porto, Portugal",
                    category = "Conference"
                ),
                Event(
                    title = "Hackathon 2024",
                    description = "A 48-hour hackathon for students and professionals to collaborate and innovate.",
                    teamMembers = accounts.subList(3, 6).toMutableList(), // Assume next 3 accounts are team members
                    associatedRoles = perActivityRoles.subList(2, 4).toMutableList(), // Next 2 roles are related
                    slug = "hackathon-2024",
                    image = "hackathon_image.png",
                    registerUrl = "https://hackathon.com/register",
                    dateInterval = DateInterval(
                        startDate = Date.from(faker.date().birthday().toInstant()),
                        endDate = Date.from(faker.date().birthday().toInstant())
                    ),
                    location = "Lisbon, Portugal",
                    category = "Hackathon"
                )
            )

            repository.saveAll(events)
            logger.info("Event data seeded successfully.")
        } else {
            logger.info("Event data already exists, skipping seeding.")
        }
    }
}
