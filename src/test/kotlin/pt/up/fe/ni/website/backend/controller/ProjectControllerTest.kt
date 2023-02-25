package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.DocumentationHelper.Companion.addFieldsToPayloadBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.EmptyObjectSchema
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema

@ControllerTest
@AutoConfigureRestDocs
internal class ProjectControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: ProjectRepository,
    val accountRepository: AccountRepository
) {

    final val testAccount = Account(
        "Test Account",
        "test_account@test.com",
        "test_password",
        "This is a test account",
        TestUtils.createDate(2001, Calendar.JULY, 28),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        )
    )

    val testProject = Project(
        "Awesome project",
        "this is a test project",
        mutableListOf(testAccount),
        false,
        listOf("Java", "Kotlin", "Spring"),
        slug = "awesome-project"
    )

    private val projectFields = listOf<FieldDescriptor>(
        fieldWithPath("title").type(JsonFieldType.STRING).description("Project title"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("Project description"),
        fieldWithPath("isArchived").type(JsonFieldType.BOOLEAN).description(
            "Whether this project is being actively maintained"
        ),
        fieldWithPath("technologies").type(JsonFieldType.ARRAY)
            .description("Array of technologies used in the project").optional(),
        fieldWithPath("associatedRoles[]").type(JsonFieldType.ARRAY).description(
            "An activity that aggregates members with different roles"
        ).optional(),
        fieldWithPath("associatedRoles[].*.permissions").type(JsonFieldType.OBJECT).description(
            "Permissions of someone with a given role for this activity"
        ).optional(),
        fieldWithPath("associatedRoles[].*.id").type(JsonFieldType.NUMBER).description(
            "Id of the role/activity association"
        ).optional(),
        fieldWithPath("slug").type(JsonFieldType.STRING)
            .description("Short and friendly textual event identifier").optional()
    )
    private val projectPayloadSchema = PayloadSchema("project", projectFields)
    private val responseOnlyProjectFields = mutableListOf<FieldDescriptor>(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Project ID"),
        fieldWithPath("teamMembers").type(JsonFieldType.ARRAY).description(
            "Array of members associated with the project"
        )
    ).addFieldsToPayloadBeneathPath(
        "teamMembers",
        AccountControllerTest.accountPayloadSchema.Response().arrayDocumentedFields(
            AccountControllerTest.responseOnlyAccountFields
        ),
        optional = true
    )

    private val requestOnlyProjectFields = listOf<FieldDescriptor>(
        fieldWithPath("teamMembersIds").type(JsonFieldType.ARRAY).description(
            "Array with IDs of members associated with the project"
        ),
        fieldWithPath("teamMembersIds.*").type(JsonFieldType.NUMBER).description("Account ID").optional()
    )

    @NestedTest
    @DisplayName("GET /projects")
    inner class GetAllProjects {
        private val testProjects = listOf(
            testProject,
            Project(
                "NIJobs",
                "Job platform for students",
                mutableListOf(),
                false,
                listOf("ExpressJS", "React")
            )
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            for (project in testProjects) repository.save(project)
        }

        @Test
        fun `should return all projects`() {
            mockMvc.perform(get("/projects"))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    content().json(objectMapper.writeValueAsString(testProjects))
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Get all the events")
                                    .description(
                                        """
                                        The operation returns an array of projects, allowing to easily retrieve all the created projects.
                                        This is useful for example in the frontend project page, where projects are displayed.
                                        """.trimIndent()
                                    )
                                    .responseSchema(projectPayloadSchema.Response().arraySchema())
                                    .responseFields(
                                        projectPayloadSchema.Response().arrayDocumentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    @NestedTest
    @DisplayName("GET /projects/{projectId}")
    inner class GetProjectById {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should return the project`() {
            mockMvc.perform(get("/projects/{id}", testProject.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testProject.title),
                    jsonPath("$.description").value(testProject.description),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
                    jsonPath("$.slug").value(testProject.slug)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Get projects by ID")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of a single project using its ID.
                                        It might be used to generate the specific project page.
                                        """.trimIndent()
                                    )
                                    .pathParameters(
                                        parameterWithName("id").description("ID of the project to retrieve")
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.perform(get("/projects/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(
                                        parameterWithName("id").description("ID of the project to retrieve")
                                    )
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    @NestedTest
    @DisplayName("GET /projects/{projectSlug}")
    inner class GetProjectBySlug {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should return the project`() {
            mockMvc.get("/projects/${testProject.slug}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.title") { value(testProject.title) }
                jsonPath("$.description") { value(testProject.description) }
                jsonPath("$.technologies.length()") { value(testProject.technologies.size) }
                jsonPath("$.technologies[0]") { value(testProject.technologies[0]) }
                jsonPath("$.slug") { value(testProject.slug) }
            }
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.get("/projects/does-not-exist").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("project not found with slug does-not-exist") }
            }
        }
    }

    @NestedTest
    @DisplayName("POST /projects/new")
    inner class CreateProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
        }

        @Test
        fun `should create a new project`() {
            mockMvc.perform(
                post("/projects/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to testProject.title,
                                "description" to testProject.description,
                                "teamMembersIds" to mutableListOf(testAccount.id!!),
                                "isArchived" to testProject.isArchived,
                                "technologies" to testProject.technologies,
                                "slug" to testProject.slug
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testProject.title),
                    jsonPath("$.description").value(testProject.description),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
                    jsonPath("$.slug").value(testProject.slug)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Create new projects")
                                    .description(
                                        """
                                        This endpoint operation creates new projects.
                                        """.trimIndent()
                                    )
                                    .requestSchema(projectPayloadSchema.Request().schema())
                                    .requestFields(
                                        projectPayloadSchema.Request().documentedFields(requestOnlyProjectFields)
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }

        @Test
        fun `should fail if slug already exists`() {
            val duplicatedSlugProject = Project(
                "Duplicated Slug",
                "this is a test project with a duplicated slug",
                mutableListOf(testAccount),
                false,
                listOf("Java", "Kotlin", "Spring"),
                slug = testProject.slug
            )

            mockMvc.post("/projects/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testProject)
            }.andExpect { status { isOk() } }

            mockMvc.post("/projects/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(duplicatedSlugProject)
            }
                .andExpect {
                    status { isUnprocessableEntity() }
                    content { MediaType.APPLICATION_JSON }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("slug already exists") }
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/projects/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params))
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "events/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .requestSchema(projectPayloadSchema.Request().schema())
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Projects")
                                            .build()
                                    )
                                )
                            )
                        )
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description
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
            @DisplayName("description")
            inner class DescriptionValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "description"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName(
                    "size should be between ${Constants.Description.minSize} " +
                        "and ${Constants.Description.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
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
    @DisplayName("DELETE /projects/{projectId}")
    inner class DeleteProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should delete the project`() {
            mockMvc.perform(delete("/projects/{id}", testProject.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Delete projects")
                                    .description(
                                        """
                                        This operation deletes an projects using its ID.
                                        """.trimIndent()
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to delete"))
                                    .responseSchema(EmptyObjectSchema().Response().schema())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
            assert(repository.findById(testProject.id!!).isEmpty)
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.perform(delete("/projects/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(parameterWithName("id").description("ID of the project to delete"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}")
    inner class UpdateProject {

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should update the project without the slug`() {
            val newTitle = "New Title"
            val newDescription = "New description of the project"
            val newTeamMembers = mutableListOf<Long>()
            val newIsArchived = true

            mockMvc.perform(
                put("/projects/{id}", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "teamMembersIds" to newTeamMembers,
                                "isArchived" to newIsArchived
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(0),
                    jsonPath("$.isArchived").value(newIsArchived)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Update projects")
                                    .description(
                                        """Update previously created projects, using their ID."""
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to update"))
                                    .requestSchema(projectPayloadSchema.Request().schema())
                                    .requestFields(
                                        projectPayloadSchema.Request().documentedFields(requestOnlyProjectFields)
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
        }

        @Test
        fun `should update the project with the slug`() {
            val newTitle = "New Title"
            val newDescription = "New description of the project"
            val newIsArchived = true
            val newSlug = "new-title"

            mockMvc.put("/projects/${testProject.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "description" to newDescription,
                        "isArchived" to newIsArchived,
                        "slug" to newSlug
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(newTitle) }
                    jsonPath("$.description") { value(newDescription) }
                    jsonPath("$.isArchived") { value(newIsArchived) }
                    jsonPath("$.slug") { value(newSlug) }
                }

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
            assertEquals(newSlug, updatedProject.slug)
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.perform(
                put("/projects/{id}", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to "New Title",
                                "description" to "New description of the project"
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(parameterWithName("id").description("ID of the project to update"))
                                    .requestSchema(projectPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }

        @Test
        fun `should fail if the slug already exists`() {
            val newTitle = "New Title"
            val newDescription = "New description of the project"
            val newIsArchived = true
            val newSlug = "new-title"

            mockMvc.post("/projects/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    Project(
                        "Duplicated Slug",
                        "this is a test project with a duplicated slug",
                        mutableListOf(testAccount),
                        false,
                        listOf("Java", "Kotlin", "Spring"),
                        slug = newSlug
                    )
                )
            }.andExpect { status { isOk() } }

            mockMvc.put("/projects/${testProject.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "description" to newDescription,
                        "isArchived" to newIsArchived,
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
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        put("/projects/{id}", testProject.id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params))
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "projects/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .pathParameters(
                                                parameterWithName("id").description("ID of the project to update")
                                            )
                                            .requestSchema(projectPayloadSchema.Request().schema())
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Projects")
                                            .build()
                                    )
                                )
                            )
                        )
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description
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
            @DisplayName("description")
            inner class BodyValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "description"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName(
                    "size should be between ${Constants.Description.minSize}" +
                        " and ${Constants.Description.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
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
    @DisplayName("PUT /projects/{projectId}/archive")
    inner class ArchiveProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should archive the project`() {
            val newIsArchived = true

            mockMvc.perform(
                put("/projects/{id}/archive", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString("isArchived" to newIsArchived))
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Archive projects")
                                    .description(
                                        """This endpoint updates projects as archived. This is useful to mark no longer
                                            |maintained or complete projects of the Nucleus.
                                        """.trimMargin()
                                    )
                                    .pathParameters(
                                        parameterWithName("id")
                                            .description("ID of the project to archive")
                                    )
                                    .requestSchema(Schema("project-archive-request"))
                                    .requestFields(
                                        fieldWithPath("first").type(JsonFieldType.STRING).description(
                                            "String with property name (\"isArchived\")"
                                        ),
                                        fieldWithPath("second").type(JsonFieldType.BOOLEAN).description(
                                            "Whether the project is archived"
                                        )
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )

            val archivedProject = repository.findById(testProject.id!!).get()
            assertEquals(newIsArchived, archivedProject.isArchived)
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/unarchive")
    inner class UnarchiveProject {
        private val project = Project(
            "proj1",
            "very cool project",
            mutableListOf(),
            true,
            listOf("React", "TailwindCSS")
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(project)
        }

        @Test
        fun `should unarchive the project`() {
            val newIsArchived = false

            mockMvc.perform(
                put("/projects/{id}/unarchive", project.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString("isArchived" to newIsArchived))
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Unarchive projects")
                                    .description(
                                        """This endpoint updates projects as unarchived.
                                            |This is useful to mark previously unarchived projects as active.
                                        """.trimMargin()
                                    )
                                    .pathParameters(
                                        parameterWithName("id").description("ID of the project to unarchive")
                                    )
                                    .requestSchema(Schema("project-unarchive-request"))
                                    .requestFields(
                                        fieldWithPath("first").type(JsonFieldType.STRING).description(
                                            "String with property name (\"isArchived\")"
                                        ),
                                        fieldWithPath("second").type(JsonFieldType.BOOLEAN).description(
                                            "Whether the project is archived"
                                        )
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )

            val unarchivedProject = repository.findById(project.id!!).get()
            assertEquals(newIsArchived, unarchivedProject.isArchived)
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/addTeamMember/{accountId}")
    inner class AddTeamMember {

        private val newAccount = Account(
            "Another test Account",
            "test2_account@test.com",
            "test_password",
            "This is another test account",
            TestUtils.createDate(2003, Calendar.APRIL, 4),
            "https://test-photo.com",
            "https://linkedin.com",
            "https://github.com",
            listOf(
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
            )
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(newAccount)
            repository.save(testProject)
        }

        @Test
        fun `should add a team member`() {
            mockMvc.perform(
                put("/projects/{projectId}/addTeamMember/{accountId}", testProject.id, newAccount.id)
            )
                .andExpectAll(
                    status().isOk, content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(2),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].bio").value(testAccount.bio),
                    jsonPath("$.teamMembers[0].birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[0].photoPath").value(testAccount.photoPath),
                    jsonPath("$.teamMembers[0].linkedin").value(testAccount.linkedin),
                    jsonPath("$.teamMembers[0].github").value(testAccount.github),
                    jsonPath("$.teamMembers[0].websites.length()").value(1),
                    jsonPath("$.teamMembers[0].websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.teamMembers[0].websites[0].iconPath").value(testAccount.websites[0].iconPath),
                    jsonPath("$.teamMembers[1].name").value(newAccount.name),
                    jsonPath("$.teamMembers[1].email").value(newAccount.email),
                    jsonPath("$.teamMembers[1].bio").value(newAccount.bio),
                    jsonPath("$.teamMembers[1].birthDate").value(newAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[1].photoPath").value(newAccount.photoPath),
                    jsonPath("$.teamMembers[1].linkedin").value(newAccount.linkedin),
                    jsonPath("$.teamMembers[1].github").value(newAccount.github),
                    jsonPath("$.teamMembers[1].websites.length()").value(1),
                    jsonPath("$.teamMembers[1].websites[0].url").value(newAccount.websites[0].url),
                    jsonPath("$.teamMembers[1].websites[0].iconPath").value(newAccount.websites[0].iconPath)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Add member to Project")
                                    .description(
                                        """
                                        This operation add a member to a given project.
                                        """.trimIndent()
                                    )
                                    .pathParameters(
                                        parameterWithName("projectId").description(
                                            "ID of the project to add the member to"
                                        ),
                                        parameterWithName("accountId").description("ID of the account to add")
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/projects/{projectId}/addTeamMember/{accountId}", testProject.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(
                                        parameterWithName("projectId").description(
                                            "ID of the project to add the member to"
                                        ),
                                        parameterWithName("accountId").description("ID of the account to add")
                                    )
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/removeTeamMember/{accountId}")
    inner class RemoveTeamMember {

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        @Test
        fun `should remove a team member`() {
            mockMvc.perform(put("/projects/{projectId}/removeTeamMember/{accountId}", testProject.id, testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(0)
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Remove member from Project")
                                    .description(
                                        """
                                        This operation removes a member of a given project.
                                        """.trimIndent()
                                    )
                                    .pathParameters(
                                        parameterWithName("projectId").description(
                                            "ID of the project to remove the member from"
                                        ),
                                        parameterWithName("accountId").description("ID of the account to remove")
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(
                                        projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields)
                                    )
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/projects/{projectId}/removeTeamMember/{accountId}", testProject.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(
                                        parameterWithName("projectId").description(
                                            "ID of the project to remove the member from"
                                        ),
                                        parameterWithName("accountId").description("ID of the account to remove")
                                    )
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
