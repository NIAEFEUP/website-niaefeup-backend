package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
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
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.documentation.payloadschemas.model.Project as PayloadProject
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
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.MockMVCExtension.Companion.andDocumentCustomRequestSchema
import pt.up.fe.ni.website.backend.utils.documentation.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation
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

    val documentation: ModelDocumentation = PayloadProject()

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
                .andDocument(
                    documentation.getModelDocumentationArray(),
                    "Get all the events",
                    "The operation returns an array of projects, allowing to easily retrieve all the created " +
                        "projects. This is useful for example in the frontend project page, " +
                        "where projects are displayed."
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

        private val parameters = listOf(parameterWithName("id").description("ID of the project to retrieve"))

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
                .andDocument(
                    documentation,
                    "Get projects by ID",
                    "This endpoint allows the retrieval of a single project using its ID. " +
                        "It might be used to generate the specific project page.",
                    urlParameters = parameters
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
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
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

        private val parameters = listOf(
            parameterWithName("slug").description(
                "Short and friendly textual project identifier"
            )
        )

        @Test
        fun `should return the project`() {
            mockMvc.perform(get("/projects/{slug}", testProject.slug))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testProject.title),
                    jsonPath("$.description").value(testProject.description),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
                    jsonPath("$.slug").value(testProject.slug)
                )
                .andDocument(
                    documentation,
                    "Get projects by slug",
                    "This endpoint allows the retrieval of a single project using its slug.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.perform(get("/projects/{slug}", "does-not-exist"))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message")
                        .value("project not found with slug does-not-exist")
                )
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
                )
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
                .andDocument(
                    documentation,
                    "Create new projects",
                    "This endpoint operation creates new projects.",
                    documentRequestPayload = true
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

            mockMvc.perform(
                post("/projects/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicatedSlugProject))
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
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
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
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

        private val parameters = listOf(parameterWithName("id").description("ID of the project to delete"))

        @Test
        fun `should delete the project`() {
            mockMvc.perform(delete("/projects/{id}", testProject.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty
                )
                .andDocumentEmptyObjectResponse(
                    documentation,
                    "Delete projects",
                    "This operation deletes an projects using its ID.",
                    urlParameters = parameters
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
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
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

        val parameters = listOf(parameterWithName("id").description("ID of the project to update"))

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
                .andDocument(
                    documentation,
                    "Update projects",
                    "Update previously created projects, using their ID.",
                    urlParameters = parameters,
                    documentRequestPayload = true
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

            mockMvc.perform(
                put("/projects/{id}", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "isArchived" to newIsArchived,
                                "slug" to newSlug
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.isArchived").value(newIsArchived),
                    jsonPath("$.slug").value(newSlug)
                )
                .andDocument(
                    documentation,
                    urlParameters = parameters,
                    documentRequestPayload = true
                )

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
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters,
                    hasRequestPayload = true
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

            mockMvc.perform(
                put("/projects/{id}", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "isArchived" to newIsArchived,
                                "slug" to newSlug
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters,
                    hasRequestPayload = true
                )
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
                        .andDocumentErrorResponse(
                            documentation,
                            urlParameters = parameters,
                            hasRequestPayload = true
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

    val archivalPayload = PayloadSchema(
        "project-archival",
        mutableListOf(
            DocumentedJSONField("first", "String with property name (\"isArchived\")", JsonFieldType.STRING),
            DocumentedJSONField("second", "Whether the project is archived", JsonFieldType.BOOLEAN)
        )
    )

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/archive")
    inner class ArchiveProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testProject)
        }

        private val parameters = listOf(
            parameterWithName("id")
                .description("ID of the project to archive")
        )

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
                .andDocumentCustomRequestSchema(
                    documentation,
                    archivalPayload,
                    "Archive projects",
                    "This endpoint updates projects as archived. This is useful to mark no longer " +
                        "maintained or complete projects of the Nucleus.",
                    urlParameters = parameters,
                    documentRequestPayload = true
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

        private val parameters = listOf(parameterWithName("id").description("ID of the project to unarchive"))

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
                .andDocumentCustomRequestSchema(
                    documentation,
                    archivalPayload,
                    "Unarchive projects",
                    "This endpoint updates projects as unarchived. " +
                        "This is useful to mark previously unarchived projects as active.",
                    urlParameters = parameters,
                    documentRequestPayload = true
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

        private val parameters = listOf(
            parameterWithName("projectId").description(
                "ID of the project to add the member to"
            ),
            parameterWithName("accountId").description("ID of the account to add")
        )

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
                .andDocument(
                    documentation,
                    "Add member to Project",
                    "This operation add a member to a given project.",
                    urlParameters = parameters
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
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
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

        private val parameters = listOf(
            parameterWithName("projectId").description(
                "ID of the project to remove the member from"
            ),
            parameterWithName("accountId").description("ID of the account to remove")
        )

        @Test
        fun `should remove a team member`() {
            mockMvc.perform(put("/projects/{projectId}/removeTeamMember/{accountId}", testProject.id, testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(0)
                )
                .andDocument(
                    documentation,
                    "Remove member from Project",
                    "This operation removes a member of a given project.",
                    urlParameters = parameters
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
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
                )
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
