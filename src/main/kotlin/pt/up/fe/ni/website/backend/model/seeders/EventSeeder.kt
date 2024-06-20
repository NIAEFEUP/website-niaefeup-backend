package pt.up.fe.ni.website.backend.model.seeders

import java.util.stream.StreamSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.EventRepository

@Component
class EventSeeder(
    @Autowired val accountRepository: AccountRepository
) : AbstractSeeder<EventRepository, Event, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running event logger...")
        val accounts = StreamSupport
            .stream(accountRepository.findAll().spliterator(), false)
            .limit(10).toList()
        /*for (i in 1..10) {
            val event = Event(
                faker.lorem().sentence(4),
                faker.lorem().sentence(),
                listOf(accounts[i]).toMutableList(),
                null,
                faker.internet().image()
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
        }*/
    }
}
