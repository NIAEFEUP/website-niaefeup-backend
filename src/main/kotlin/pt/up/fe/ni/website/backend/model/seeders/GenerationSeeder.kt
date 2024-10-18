package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.repository.GenerationRepository

@Component
class GenerationSeeder() : AbstractSeeder<GenerationRepository, Generation, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running Generation seeder...")

        if (repository.count() > 0) {
            logger.info("Skipping Generation seeding as generations already exist.")
            return
        }

        val generations = listOf(
            Generation(
                schoolYear = "2020/2021"
            ),
            Generation(
                schoolYear = "2021/2022"
            )
        )

        repository.saveAll(generations)
        logger.info("Seeded ${generations.size} generations into the system.")
    }
}
