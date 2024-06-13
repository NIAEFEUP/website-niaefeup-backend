package pt.up.fe.ni.website.backend.hooks

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.seeders.ApplicationSeeder

@Component
class ApplicationStartupHook(
    val applicationSeeder: ApplicationSeeder
) : ApplicationRunner, Logging {

    @Value("\${app.debug}")
    val debug: Boolean = false

    override fun run(args: ApplicationArguments?) {
        logger.info("Running Startup hook...")
        if (debug) {
            logger.info("Running application seeder...")
            applicationSeeder.seedDatabase()
        }
    }
}
