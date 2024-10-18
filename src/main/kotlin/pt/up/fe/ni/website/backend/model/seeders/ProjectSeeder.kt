package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.CustomWebsiteRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.repository.TimeLineEventRepository

@Component
class ProjectSeeder(
    @Autowired val accountRepository: AccountRepository,
    @Autowired val perActivityRoleRepository: PerActivityRoleRepository,
    @Autowired val customWebsiteRepository: CustomWebsiteRepository,
    @Autowired val timelineEventRepository: TimeLineEventRepository
) : AbstractSeeder<ProjectRepository, Project, Long>(), Logging {

    val accounts = accountRepository.findAll().toList()
    val perActivityRoles = perActivityRoleRepository.findAll().toList()

    val customWebsites = customWebsiteRepository.findAll().toList()
    val timelineEvents = timelineEventRepository.findAll().toList()

    override fun createObjects() {
        if (repository.count() == 0L) {
            logger.info("Seeding Project data...")

            val projects = listOf(
                Project(
                    title = "AI Research Project",
                    description = "A project focused on developing AI models for real-time data analysis.",
                    teamMembers = accounts.subList(0, 3).toMutableList(),
                    associatedRoles = perActivityRoles.subList(0, 2).toMutableList(),
                    slug = "ai-research-project",
                    image = "ai_project_image.png",
                    isArchived = false,
                    technologies = listOf("Python", "TensorFlow", "Keras"),
                    slogan = "Transforming data with AI",
                    targetAudience = "Researchers, Data Scientists",
                    github = "https://github.com/research-ai",
                    links = customWebsites.subList(0, 2),
                    hallOfFame = accounts.subList(3, 4).toMutableList(),
                    timeline = timelineEvents.subList(0, 2)
                ),
                Project(
                    title = "Blockchain Development Project",
                    description = "Building a decentralized application using blockchain technology.",
                    teamMembers = accounts.subList(3, 6).toMutableList(),
                    associatedRoles = perActivityRoles.subList(2, 4).toMutableList(),
                    slug = "blockchain-dev-project",
                    image = "blockchain_project_image.png",
                    isArchived = false,
                    technologies = listOf("Solidity", "Ethereum", "Web3"),
                    slogan = "Decentralizing the future",
                    targetAudience = "Developers, Fintech Enthusiasts",
                    github = "https://github.com/blockchain-dev",
                    links = customWebsites.subList(2, 4),
                    hallOfFame = accounts.subList(6, 7).toMutableList(),
                    timeline = timelineEvents.subList(2, 4)
                )
            )

            repository.saveAll(projects)
            logger.info("Project data seeded successfully.")
        } else {
            logger.info("Project data already exists, skipping seeding.")
        }
    }
}
