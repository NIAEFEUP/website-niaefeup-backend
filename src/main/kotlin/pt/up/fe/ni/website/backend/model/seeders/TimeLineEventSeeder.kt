package pt.up.fe.ni.website.backend.model.seeders

import java.util.Date
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.TimelineEvent
import pt.up.fe.ni.website.backend.repository.TimeLineEventRepository

@Component
class TimeLineEventSeeder() : AbstractSeeder<TimeLineEventRepository, TimelineEvent, Long>(), Logging {

    override fun createObjects() {
        if (repository.count() == 0L) {
            logger.info("Seeding TimelineEvent data...")

            val timelineEvents = listOf(
                TimelineEvent(
                    date = Date(1672531200000),
                    description = "Project Conceptualization"
                ),
                TimelineEvent(
                    date = Date(1675123200000),
                    description = "First Prototype Developed"
                ),
                TimelineEvent(
                    date = Date(1677801600000),
                    description = "Initial User Testing"
                ),
                TimelineEvent(
                    date = Date(1680476400000), // Example: April 1, 2023
                    description = "Launch of Beta Version"
                )
            )

            repository.saveAll(timelineEvents)
            logger.info("TimelineEvent data seeded successfully.")
        } else {
            logger.info("TimelineEvent data already exists, skipping seeding.")
        }
    }
}
