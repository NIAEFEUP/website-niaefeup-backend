package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.EmptyObjectSchema
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema
import java.text.SimpleDateFormat
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants

@ControllerTest
@AutoConfigureRestDocs
internal class PostControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: PostRepository,
) {
    val testPost = Post(
        "New test released",
        "this is a test post",
        "https://thumbnails/test.png",
        slug = "new-test-released",
    )

    private val postFields = listOf<FieldDescriptor>(
        PayloadDocumentation.fieldWithPath("title").type(JsonFieldType.STRING).description("Post title"),
        PayloadDocumentation.fieldWithPath("body").type(JsonFieldType.STRING).description("Post body"),
        PayloadDocumentation.fieldWithPath("thumbnailPath").type(JsonFieldType.STRING).description("Path for the post thumbnail image"),
        PayloadDocumentation.fieldWithPath("slug").type(JsonFieldType.STRING).description("Short and friendly textual post identifier").optional(),
    )
    private val postPayloadSchema = PayloadSchema("post", postFields)
    private val responseOnlyPostFields = listOf<FieldDescriptor>(
        PayloadDocumentation.fieldWithPath("id").type(JsonFieldType.NUMBER).description("Post ID"),
        PayloadDocumentation.fieldWithPath("publishDate").type(JsonFieldType.STRING).description("Date of publication of the post"),
        PayloadDocumentation.fieldWithPath("lastUpdatedAt").type(JsonFieldType.STRING).description("Date of the last update of the post"),
    )

    @NestedTest
    @DisplayName("GET /posts")
    inner class GetAllPosts {
        private val testPosts = listOf(
            testPost,
            Post(
                "NIAEFEUP gets a new president",
                "New president promised to buy new chairs",
                "https://thumbnails/pres.png",
            ),
        )

        @BeforeEach
        fun addPosts() {
            for (post in testPosts) repository.save(post)
        }

        @Test
        fun `should return all posts`() {
            mockMvc.perform(get("/posts"))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    content().json(
                        objectMapper.writeValueAsString(testPosts),
                    ),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Get all the posts")
                                    .description(
                                        """
                                        The operation returns an array of posts, allowing to easily retrieve all the created posts.
                                        """.trimIndent(),
                                    )
                                    .responseSchema(postPayloadSchema.Response().arraySchema())
                                    .responseFields(postPayloadSchema.Response().arrayDocumentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
            mockMvc.perform(get("/posts/{id}", testPost.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testPost.title),
                    jsonPath("$.body").value(testPost.body),
                    jsonPath("$.thumbnailPath").value(testPost.thumbnailPath),
                    jsonPath("$.publishDate").value(testPost.publishDate.toJson()),
                    jsonPath("$.lastUpdatedAt").value(testPost.lastUpdatedAt.toJson(true)),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Get posts by ID")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of a single post using its ID.
                                        It might be used to generate the specific post page.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to retrieve"))
                                    .responseSchema(postPayloadSchema.Response().schema())
                                    .responseFields(postPayloadSchema.Response().documentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.perform(get("/posts/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("post not found with id 1234"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to retrieve"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
            mockMvc.perform(get("/posts/{slug}", testPost.slug))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(testPost.id),
                    jsonPath("$.title").value(testPost.title),
                    jsonPath("$.body").value(testPost.body),
                    jsonPath("$.thumbnailPath").value(testPost.thumbnailPath),
                    jsonPath("$.publishDate").value(testPost.publishDate.toJson()),
                    jsonPath("$.lastUpdatedAt").value(testPost.lastUpdatedAt.toJson(true)),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Get posts by slug")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of a single post using its slug.
                                        It might be used to generate the specific post page.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(ResourceDocumentation.parameterWithName("slug").description("Short and friendly textual post identifier"))
                                    .responseSchema(postPayloadSchema.Response().schema())
                                    .responseFields(postPayloadSchema.Response().documentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.perform(get("/posts/{slug}", "fail-slug"))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("post not found with slug fail-slug"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("slug").description("Short and friendly textual post identifier"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
            mockMvc.perform(
                post("/posts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testPost)),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testPost.title),
                    jsonPath("$.body").value(testPost.body),
                    jsonPath("$.thumbnailPath").value(testPost.thumbnailPath),
                    jsonPath("$.publishDate").exists(),
                    jsonPath("$.lastUpdatedAt").exists(),
                    jsonPath("$.slug").value(testPost.slug),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Create new posts")
                                    .description(
                                        """This endpoint operation creates new posts.""".trimIndent(),
                                    )
                                    .requestSchema(postPayloadSchema.Request().schema())
                                    .requestFields(postPayloadSchema.Request().documentedFields())
                                    .responseSchema(postPayloadSchema.Response().schema())
                                    .responseFields(postPayloadSchema.Response().documentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail to create post with existing slug`() {
            mockMvc.post("/posts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testPost)
            }.andExpect { status { isOk() } }

            mockMvc.perform(
                post("/posts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testPost)),
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/posts/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "posts/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Posts")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "title" to testPost.title,
                    "body" to testPost.body,
                    "thumbnailPath" to testPost.thumbnailPath,
                ),
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
            mockMvc.perform(delete("/posts/{id}", testPost.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty,
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Delete posts")
                                    .description(
                                        """
                                        This operation deletes an event using its ID.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to delete"))
                                    .responseSchema(EmptyObjectSchema().Response().schema())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )

            assert(repository.findById(testPost.id!!).isEmpty)
        }

        @Test
        fun `should fail if the post does not exist`() {
            mockMvc.perform(delete("/posts/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("post not found with id 1234"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to delete"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
                    slug = "duplicated-slug",
                ),
            )
        }

        @Test
        fun `should update the post without the slug`() {
            val newTitle = "New Title"
            val newBody = "New Body of the post"
            val newThumbnailPath = "https://thumbnails/new.png"

            mockMvc.perform(
                put("/posts/{id}", testPost.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "body" to newBody,
                                "thumbnailPath" to newThumbnailPath,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.body").value(newBody),
                    jsonPath("$.thumbnailPath").value(newThumbnailPath),
                    jsonPath("$.publishDate").value(testPost.publishDate.toJson()),
                    jsonPath("$.lastUpdatedAt").exists(),
                    jsonPath("$.slug").value(testPost.slug),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Update posts")
                                    .description(
                                        """Update previously created posts, using their ID.""",
                                    )
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to update"))
                                    .requestSchema(postPayloadSchema.Request().schema())
                                    .requestFields(postPayloadSchema.Request().documentedFields())
                                    .responseSchema(postPayloadSchema.Response().schema())
                                    .responseFields(postPayloadSchema.Response().documentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )

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

            mockMvc.perform(
                put("/posts/{id}", testPost.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "body" to newBody,
                                "thumbnailPath" to newThumbnailPath,
                                "slug" to newSlug,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.body").value(newBody),
                    jsonPath("$.thumbnailPath").value(newThumbnailPath),
                    jsonPath("$.publishDate").value(testPost.publishDate.toJson()),
                    jsonPath("$.lastUpdatedAt").exists(),
                    jsonPath("$.slug").value(newSlug),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to update"))
                                    .responseSchema(postPayloadSchema.Response().schema())
                                    .responseFields(postPayloadSchema.Response().documentedFields(responseOnlyPostFields))
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )

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
            mockMvc.perform(
                put("/posts/{id}", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to "New Title",
                                "body" to "New Body of the post",
                                "thumbnailPath" to "thumbnails/new.png",
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("post not found with id 1234"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to update"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the slug already exist`() {
            val newTitle = "New Title"
            val newBody = "New Body of the post"
            val newThumbnailPath = "https://thumbnails/new.png"
            val newSlug = "duplicated-slug"

            mockMvc.perform(
                put("/posts/{id}", testPost.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "body" to newBody,
                                "thumbnailPath" to newThumbnailPath,
                                "slug" to newSlug,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "posts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to update"))
                                    .requestSchema(postPayloadSchema.Request().schema())
                                    .requestFields(postPayloadSchema.Request().documentedFields())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Posts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        put("/posts/{id}", testPost.id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "posts/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the post to update"))
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Posts")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "title" to testPost.title,
                    "body" to testPost.body,
                    "thumbnailPath" to testPost.thumbnailPath,
                ),
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
