package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository

@Component
class ProjectSeeder() : AbstractSeeder<ProjectRepository, Project, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running project seeder...")
        for (i in 1..10) {
//            val project = Project(
//                title = faker.lorem().word(),
//                description = faker.lorem().paragraph(),
//                teamMembers = ,
//                associatedRoles = ,
//                slug = faker.internet().slug(),
//                image = faker.internet().image(),
//                isArchived = faker.random().nextBoolean(),
//                technologies = List(faker.random().nextInt()) {faker.lorem().word()},
//                slogan = faker.lorem().characters(ProjectConstants.Slogan.minSize, ProjectConstants.Slogan.maxSize, true, true, true),
//                targetAudience = faker.lorem().sentence(),
//                github = faker.internet().url(),
//                links = ,
//                hallOfFame = ,
//                timeline = ,
//            )
        }
    }
}
