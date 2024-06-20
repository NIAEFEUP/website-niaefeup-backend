package pt.up.fe.ni.website.backend.model.seeders

import java.util.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.repository.AccountRepository

@Component
class AccountSeeder(
    private val encoder: PasswordEncoder
) : AbstractSeeder<AccountRepository, Account, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running account seeder...")
        for (i in 1..10) {
            val account = Account(
                faker.name().firstName(),
                faker.internet().emailAddress(),
                encoder.encode(faker.random().hex(16)),
                faker.lorem().sentence(),
                Date.from(faker.date().birthday().toInstant()),
                photo = faker.internet().image(),
                github = null,
                linkedin = null
            )
            val accountWithSocials = Account(
                faker.name().firstName(),
                faker.internet().emailAddress(),
                encoder.encode(faker.random().hex(16)),
                faker.lorem().sentence(),
                Date.from(faker.date().birthday().toInstant()),
                photo = faker.internet().image(),
                github = faker.internet().url(),
                linkedin = faker.internet().url()
            )
            val accountWithGithub = Account(
                faker.name().firstName(),
                faker.internet().emailAddress(),
                encoder.encode(faker.random().hex(16)),
                faker.lorem().sentence(),
                Date.from(faker.date().birthday().toInstant()),
                photo = faker.internet().image(),
                github = faker.internet().url(),
                linkedin = null
            )
            val accountWithLinkedin = Account(
                faker.name().firstName(),
                faker.internet().emailAddress(),
                encoder.encode(faker.random().hex(16)),
                faker.lorem().sentence(),
                Date.from(faker.date().birthday().toInstant()),
                photo = faker.internet().image(),
                github = null,
                linkedin = faker.internet().url()
            )

            repository.saveAll(
                listOf(account, accountWithLinkedin, accountWithSocials, accountWithGithub, accountWithLinkedin)
            )
        }
    }
}
