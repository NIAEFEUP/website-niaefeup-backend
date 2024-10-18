package pt.up.fe.ni.website.backend.model.seeders

import java.util.*
import org.springframework.stereotype.Component
import pt.up.fe.ni.website.backend.config.Logging
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.model.constants.PostConstants
import pt.up.fe.ni.website.backend.repository.PostRepository

@Component
class PostSeeder() : AbstractSeeder<PostRepository, Post, Long>(), Logging {

    override fun createObjects() {
        logger.info("Running post seeder...")
        for (i in 1..10) {
            val post = Post(
                faker.lorem().characters(PostConstants.Title.minSize, PostConstants.Title.maxSize, true, true, true),
                faker.lorem().characters(PostConstants.Body.minSize, 500, true, true, true),
                faker.internet().url(),
                Date.from(faker.date().birthday().toInstant()),
                Date.from(faker.date().birthday().toInstant()),
                slug = faker.lorem().characters(PostConstants.Slug.minSize, PostConstants.Slug.maxSize)
            )

            val postWithoutDate = Post(
                faker.lorem().characters(PostConstants.Title.minSize, PostConstants.Title.maxSize, true, true, true),
                faker.lorem().characters(PostConstants.Body.minSize, 500, true, true, true),
                faker.internet().url(),
                slug = faker.lorem().characters(
                    PostConstants.Slug.minSize,
                    PostConstants.Slug.maxSize,
                    false,
                    false,
                    false
                )
            )

            repository.saveAll(listOf(post, postWithoutDate))
        }
    }
}
