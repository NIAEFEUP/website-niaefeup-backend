package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.repository.CustomWebsiteRepository

@Component
class CustomWebsiteSeeder() : AbstractSeeder<CustomWebsiteRepository, CustomWebsite, Long>(), Logging {

    override fun createObjects() {
        if (repository.count() == 0L) {
            logger.info("Seeding CustomWebsite data...")

            val customWebsites = listOf(
                CustomWebsite(
                    url = "https://ai-project.com",
                    iconPath = "/images/icons/ai_project.png",
                    label = "AI Project Website"
                ),
                CustomWebsite(
                    url = "https://ai-project.com/docs",
                    iconPath = "/images/icons/docs.png",
                    label = "AI Project Documentation"
                ),
                CustomWebsite(
                    url = "https://blockchain-project.com",
                    iconPath = "/images/icons/blockchain_project.png",
                    label = "Blockchain Project Website"
                ),
                CustomWebsite(
                    url = "https://blockchain-project.com/whitepaper",
                    iconPath = "/images/icons/whitepaper.png",
                    label = "Blockchain Project Whitepaper"
                )
            )

            repository.saveAll(customWebsites)
            logger.info("CustomWebsite data seeded successfully.")
        } else {
            logger.info("CustomWebsite data already exists, skipping seeding.")
        }
    }
}
