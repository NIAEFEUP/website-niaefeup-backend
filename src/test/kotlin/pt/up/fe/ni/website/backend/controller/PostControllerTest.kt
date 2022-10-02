package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository
import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
internal class PostControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: PostRepository
) {
    val testPost = Post(
        "New test released",
        "this is a test post",
        "thumbnails/test.png"
    )

    @Nested
    @DisplayName("GET /posts")
    inner class GetAllPosts {
        private val testPosts = listOf(
            testPost,
            Post(
                "NIAEFEUP gets a new president",
                "New president promised to buy new chairs",
                "thumbnails/pres.png"
            )
        )

        @BeforeEach
        fun addEvents() {
            for (post in testPosts) repository.save(post)
        }

        @Test
        fun `should return all posts`() {
            mockMvc.get("/posts").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(testPosts)) }
            }
        }
    }

    @Nested
    @DisplayName("GET /posts/{postId}")
    inner class GetPost {
        @BeforeEach
        fun addEvent() {
            repository.save(testPost)
        }

        @Test
        fun `should return the post`() {
            mockMvc.get("/posts/${testPost.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.title") { value(testPost.title) }
                jsonPath("$.body") { value(testPost.body) }
                jsonPath("$.thumbnailPath") { value(testPost.thumbnailPath) }
                jsonPath("$.publishDate") { value(testPost.publishDate.toJson()) }
                jsonPath("$.lastUpdatedAt") { value(testPost.lastUpdatedAt.toJson()) }
            }
        }

        @Test
        fun `fail if the post does not exist`() {
            mockMvc.get("/posts/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("post not found with id 1234") }
            }
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
