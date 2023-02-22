package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import java.text.SimpleDateFormat
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.NestedTestConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants
import pt.up.fe.ni.website.backend.repository.PostRepository
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest

@ControllerTest
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

    @NestedTest
    @DisplayName("GET /posts")
    inner class GetAllPosts {
        private val testPosts = listOf(
            testPost,
            Post(
                "NIAEFEUP gets a new president",
                "New president promised to buy new chairs",
                "https://thumbnails/pres.png"
            )
        )

        @BeforeEach
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

    @NestedTest
    @DisplayName("GET /posts/{postId}")
    inner class GetPostById {
        @BeforeEach
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

    @NestedTest
    @DisplayName("GET /posts/{postSlug}")
    inner class GetPostBySlang {
        @BeforeEach
        fun addPost() {
            repository.save(testPost)
        }

        @Test
        fun `should return the post`() {
            mockMvc.get("/posts/${testPost.slug}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id") { value(testPost.id) }
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

    @NestedTest
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
                    jsonPath("$.slug") { value(testPost.slug) }
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

        @NestedTest
        @DisplayName("Input Validation")
        @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
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

            @NestedTest
            @DisplayName("title")
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

            @NestedTest
            @DisplayName("body")
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

            @NestedTest
            @DisplayName("thumbnailPath")
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

            @NestedTest
            @DisplayName("slug")
            inner class SlugValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slug"
                }

                @Test
                @DisplayName("size should be between ${Constants.Slug.minSize} and ${Constants.Slug.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Slug.minSize, Constants.Slug.maxSize)
            }
        }
    }

    @NestedTest
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

    @NestedTest
    @DisplayName("PUT /posts/{postId}")
    inner class UpdatePost {
        @BeforeEach
        fun addPost() {
            repository.save(testPost)
            repository.save(
                Post(
                    "New test released",
                    "this is a test post",
                    "https://thumbnails/test.png",
                    slug = "duplicated-slug"
                )
            )
        }

        @Test
        fun `should update the post without the slug`() {
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
                    jsonPath("$.slug") { value(testPost.slug) }
                }

            val updatedPost = repository.findById(testPost.id!!).get()
            assertEquals(newTitle, updatedPost.title)
            assertEquals(newBody, updatedPost.body)
            assertEquals(newThumbnailPath, updatedPost.thumbnailPath)
            assertEquals(testPost.slug, updatedPost.slug)
        }

        @Test
        fun `should update the post with the slug`() {
            val newTitle = "New Title"
            val newBody = "New Body of the post"
            val newThumbnailPath = "https://thumbnails/new.png"
            val newSlug = "new-slug"

            mockMvc.put("/posts/${testPost.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "body" to newBody,
                        "thumbnailPath" to newThumbnailPath,
                        "slug" to newSlug
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
                    jsonPath("$.slug") { value(newSlug) }
                }

            val updatedPost = repository.findById(testPost.id!!).get()
            assertEquals(newTitle, updatedPost.title)
            assertEquals(newBody, updatedPost.body)
            assertEquals(newThumbnailPath, updatedPost.thumbnailPath)
            assertEquals(testPost.publishDate, updatedPost.publishDate)
            assertNotEquals(testPost.lastUpdatedAt, updatedPost.lastUpdatedAt)
            assertEquals(newSlug, updatedPost.slug)
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

        @Test
        fun `should fail if the slug already exist`() {
            val newTitle = "New Title"
            val newBody = "New Body of the post"
            val newThumbnailPath = "https://thumbnails/new.png"
            val newSlug = "duplicated-slug"

            mockMvc.put("/posts/${testPost.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "body" to newBody,
                        "thumbnailPath" to newThumbnailPath,
                        "slug" to newSlug
                    )
                )
            }
                .andExpect {
                    status { isUnprocessableEntity() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("slug already exists") }
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
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

            @NestedTest
            @DisplayName("title")
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

            @NestedTest
            @DisplayName("body")
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

            @NestedTest
            @DisplayName("thumbnailPath")
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

            @NestedTest
            @DisplayName("slug")
            inner class SlugValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slug"
                }

                @Test
                @DisplayName("size should be between ${Constants.Slug.minSize} and ${Constants.Slug.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Slug.minSize, Constants.Slug.maxSize)
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
