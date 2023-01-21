package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.NestedTestConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository
import pt.up.fe.ni.website.backend.utils.ValidationTester
import java.text.SimpleDateFormat
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
internal class PostControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: PostRepository
) {
    val testPost = Post(
        "New test released",
        "this is a test post",
        "https://thumbnails/test.png",
        slug = "new-test-released"
    )

    @Nested
    @DisplayName("GET /posts")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAllPosts {
        private val testPosts = listOf(
            testPost,
            Post(
                "NIAEFEUP gets a new president",
                "New president promised to buy new chairs",
                "https://thumbnails/pres.png"
            )
        )

        @BeforeAll
        fun addPosts() {
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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetPostById {
        @BeforeAll
        fun addPost() {
            repository.save(testPost)
        }

        @Test
        fun `should return the post`() {
            mockMvc.get("/posts/${testPost.id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testPost.title) }
                    jsonPath("$.body") { value(testPost.body) }
                    jsonPath("$.thumbnailPath") { value(testPost.thumbnailPath) }
                    jsonPath("$.publishDate") { value(testPost.publishDate.toJson()) }
                    jsonPath("$.lastUpdatedAt") { value(testPost.lastUpdatedAt.toJson(true)) }
                }
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.get("/posts/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("post not found with id 1234") }
            }
        }
    }

    @Nested
    @DisplayName("GET /posts/{postSlug}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetPostBySlug {
        @BeforeAll
        fun addPost() {
            repository.save(testPost)
        }

        @Test
        fun `should return the post`() {
            mockMvc.get("/posts/${testPost.slug}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testPost.title) }
                    jsonPath("$.body") { value(testPost.body) }
                    jsonPath("$.thumbnailPath") { value(testPost.thumbnailPath) }
                    jsonPath("$.publishDate") { value(testPost.publishDate.toJson()) }
                    jsonPath("$.lastUpdatedAt") { value(testPost.lastUpdatedAt.toJson(true)) }
                }
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.get("/posts/fail-slug").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("post not found with slug fail-slug") }
            }
        }
    }

    @Nested
    @DisplayName("POST /posts/new")
    inner class CreatePost {
        @BeforeEach
        fun clearPosts() {
            repository.deleteAll()
        }

        @Test
        fun `should create a new post`() {
            mockMvc.post("/posts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testPost)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testPost.title) }
                    jsonPath("$.body") { value(testPost.body) }
                    jsonPath("$.thumbnailPath") { value(testPost.thumbnailPath) }
                    jsonPath("$.publishDate") { exists() }
                    jsonPath("$.lastUpdatedAt") { exists() }
                }
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.post("/posts/new") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "title" to testPost.title,
                    "body" to testPost.body,
                    "thumbnailPath" to testPost.thumbnailPath
                )
            )

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("title")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class TitleValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "title"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Title.minSize} and ${Constants.Title.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Title.minSize, Constants.Title.maxSize)
            }

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("body")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class BodyValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "body"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size must be greater or equal to ${Constants.Body.minSize}()")
                fun size() = validationTester.hasMinSize(Constants.Body.minSize)
            }

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("thumbnailPath")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class ThumbnailValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "thumbnailPath"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should not be empty`() = validationTester.isNotEmpty()
            }
        }

        @Test
        fun `should fail to create post with existing slug`() {
            mockMvc.post("/posts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testPost)
            }.andExpect { status { isOk() } }

            mockMvc.post("/posts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testPost)
            }.andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("slug already exists") }
            }
        }
    }

    @Nested
    @DisplayName("DELETE /posts/{postId}")
    inner class DeletePost {
        @BeforeEach
        fun addPost() {
            repository.save(testPost)
        }

        @Test
        fun `should delete the post`() {
            mockMvc.delete("/posts/${testPost.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$") { isEmpty() }
            }

            assert(repository.findById(testPost.id!!).isEmpty)
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.delete("/posts/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("post not found with id 1234") }
            }
        }
    }

    @Nested
    @DisplayName("PUT /posts/{postId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class UpdatePost {
        @BeforeAll
        fun addPost() {
            repository.save(testPost)
        }

        @Test
        fun `should update the post`() {
            val newTitle = "New Title"
            val newBody = "New Body of the post"
            val newThumbnailPath = "https://thumbnails/new.png"

            mockMvc.put("/posts/${testPost.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "body" to newBody,
                        "thumbnailPath" to newThumbnailPath
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(newTitle) }
                    jsonPath("$.body") { value(newBody) }
                    jsonPath("$.thumbnailPath") { value(newThumbnailPath) }
                    jsonPath("$.publishDate") { value(testPost.publishDate.toJson()) }
                    jsonPath("$.lastUpdatedAt") { exists() }
                }

            val updatedPost = repository.findById(testPost.id!!).get()
            assertEquals(newTitle, updatedPost.title)
            assertEquals(newBody, updatedPost.body)
            assertEquals(newThumbnailPath, updatedPost.thumbnailPath)
            assertEquals(testPost.publishDate, updatedPost.publishDate)
            assertNotEquals(testPost.lastUpdatedAt, updatedPost.lastUpdatedAt)
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.put("/posts/1234") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to "New Title",
                        "body" to "New Body of the post",
                        "thumbnailPath" to "thumbnails/new.png"
                    )
                )
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("post not found with id 1234") }
                }
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.put("/posts/${testPost.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "title" to testPost.title,
                    "body" to testPost.body,
                    "thumbnailPath" to testPost.thumbnailPath
                )
            )

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("title")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class TitleValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "title"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Title.minSize} and ${Constants.Title.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Title.minSize, Constants.Title.maxSize)
            }

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("body")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class BodyValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "body"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size must be greater or equal to ${Constants.Body.minSize}()")
                fun size() = validationTester.hasMinSize(Constants.Body.minSize)
            }

            @Nested
            @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
            @DisplayName("thumbnailPath")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class ThumbnailValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "thumbnailPath"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should not be empty`() = validationTester.isNotEmpty()
            }
        }
    }

    fun Date?.toJson(includeHour: Boolean = false): String {
        val dateMapper = objectMapper.copy()
        if (includeHour) dateMapper.dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

        val quotedDate = dateMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
