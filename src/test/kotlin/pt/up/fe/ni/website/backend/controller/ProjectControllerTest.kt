package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.config.upload.UploadConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.TimelineEvent
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadProject
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation
import pt.up.fe.ni.website.backend.utils.mockmvc.multipartBuilder

@ControllerTest
internal class ProjectControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: ProjectRepository,
    val accountRepository: AccountRepository,
    val uploadConfigProperties: UploadConfigProperties
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "Test")
        )
    )

    final val testAccount2 = Account(
        "Test Old Account",
        "test_account_old@test.com",
        "test_password",
        "This is an old test account",
        TestUtils.createDate(1994, Calendar.JUNE, 19),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "Test")
        )
    )

    val testProject = Project(
        "Awesome project",
        "this is a test project",
        mutableListOf(testAccount),
        mutableListOf(),
        "awesome-project",
        "cool-image.png",
        false,
        listOf("Java", "Kotlin", "Spring"),
        "Nice one",
        "students",
        "https://github.com/NIAEFEUP/website-niaefeup-backend",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "Test")
        ),
        mutableListOf(testAccount2),
        listOf(
            TimelineEvent(TestUtils.createDate(2020, 7, 28), "This is a new event"),
            TimelineEvent(TestUtils.createDate(2001, 2, 12), "This is an old event"),
            TimelineEvent(TestUtils.createDate(2010, 2, 12), "This is a middle event")
        )
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
                mutableListOf(),
                null,
                "cool-image.png",
                false,
                listOf("ExpressJS", "React"),
                "Nice one",
                "students",
                "https://github.com/NIAEFEUP/nijobs-fe"
            )
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
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
                    "Get all the projects",
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
            accountRepository.save(testAccount2)
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
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
                    jsonPath("$.slug").value(testProject.slug),
                    jsonPath("$.slogan").value(testProject.slogan),
                    jsonPath("$.targetAudience").value(testProject.targetAudience),
                    jsonPath("$.github").value(testProject.github),
                    jsonPath("$.image").value(testProject.image),
                    jsonPath("$.links.length()").value(testProject.links.size),
                    jsonPath("$.links[0].url").value(testProject.links[0].url),
                    jsonPath("$.timeline.length()").value(testProject.timeline.size)
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
        fun `should return the timeline ordered by date`() {
            mockMvc.perform(get("/projects/{id}", testProject.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.timeline.length()").value(testProject.timeline.size),
                    jsonPath("$.timeline[0].description").value("This is an old event"),
                    jsonPath("$.timeline[1].description").value("This is a middle event"),
                    jsonPath("$.timeline[2].description").value("This is a new event")
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
            accountRepository.save(testAccount2)
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
    @DisplayName("POST /projects")
    inner class CreateProject {
        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)
        private val expectedImagePath = "${uploadConfigProperties.staticServe}/projects/${testProject.title}-$uuid.jpeg"

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
        }

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @Test
        fun `should create a new project`() {
            val projectPart = objectMapper.writeValueAsString(
                mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description,
                    "teamMembersIds" to mutableListOf(testAccount.id!!),
                    "hallOfFameIds" to mutableListOf(testAccount2.id!!),
                    "isArchived" to testProject.isArchived,
                    "technologies" to testProject.technologies,
                    "slug" to testProject.slug,
                    "targetAudience" to testProject.targetAudience,
                    "github" to testProject.github,
                    "links" to testProject.links,
                    "timeline" to testProject.timeline,
                    "slogan" to testProject.slogan
                )
            )

            mockMvc.multipartBuilder("/projects")
                .addPart("project", projectPart)
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testProject.title),
                    jsonPath("$.description").value(testProject.description),
                    jsonPath("$.hallOfFame.length()").value(1),
                    jsonPath("$.hallOfFame[0].email").value(testAccount2.email),
                    jsonPath("$.hallOfFame[0].name").value(testAccount2.name),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
                    jsonPath("$.slug").value(testProject.slug),
                    jsonPath("$.slogan").value(testProject.slogan),
                    jsonPath("$.targetAudience").value(testProject.targetAudience),
                    jsonPath("$.github").value(testProject.github),
                    jsonPath("$.image").value(expectedImagePath),
                    jsonPath("$.links.length()").value(testProject.links.size),
                    jsonPath("$.links[0].url").value(testProject.links[0].url),
                    jsonPath("$.timeline.length()").value(testProject.timeline.size),
                    jsonPath("$.timeline[0].description").value(testProject.timeline[0].description)
                )
//                .andDocument(
//                    documentation,
//                    "Create new projects",
//                    "This endpoint operation creates a new project.",
//                    documentRequestPayload = true
//                )
        }

        @Test
        fun `should fail if slug already exists`() {
            val duplicatedSlugProject = Project(
                "Duplicated Slug",
                "this is a test project with a duplicated slug",
                mutableListOf(),
                mutableListOf(),
                testProject.slug,
                "cool-project.png",
                false,
                listOf("Java", "Kotlin", "Spring"),
                "Nice project",
                "students",
                "https://github.com/NIAEFEUP/website-niaefeup-backend",
                mutableListOf(),
                mutableListOf(testAccount)
            )

            mockMvc.multipartBuilder("/projects")
                .addPart("project", objectMapper.writeValueAsString(testProject))
                .addFile(name = "image")
                .perform()
                .andExpect { status().isOk }

            mockMvc.multipartBuilder("/projects")
                .addPart("project", objectMapper.writeValueAsString(duplicatedSlugProject))
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to create project with invalid filename extension`() {
            mockMvc.multipartBuilder("/projects")
                .addPart("project", objectMapper.writeValueAsString(testProject))
                .addFile(name = "image", filename = "image.pdf")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("createProject.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to create project with invalid filename media type`() {
            mockMvc.multipartBuilder("/projects")
                .addPart("project", objectMapper.writeValueAsString(testProject))
                .addFile(name = "image", contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("createProject.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail when missing project part`() {
            mockMvc.multipartBuilder("/projects")
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("required"),
                    jsonPath("$.errors[0].param").value("project")
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.multipartBuilder("/projects")
                        .addPart("project", objectMapper.writeValueAsString(params))
                        .addFile(name = "image")
                        .perform()
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description,
                    "targetAudience" to testProject.targetAudience
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Title.minSize} and ${ActivityConstants.Title.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
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
                    "size should be between ${ActivityConstants.Description.minSize} " +
                        "and ${ActivityConstants.Description.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Description.minSize,
                        ActivityConstants.Description.maxSize
                    )
            }

            @NestedTest
            @DisplayName("slug")
            inner class SlugValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slug"
                }

                @Test
                @DisplayName(
                    "size should be between ${ActivityConstants.Slug.minSize} and ${ActivityConstants.Slug.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Slug.minSize, ActivityConstants.Slug.maxSize)
            }

            @NestedTest
            @DisplayName("slogan")
            inner class SloganValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slogan"
                }

                @Test
                @DisplayName("size should be between ${Constants.Slogan.minSize} and ${Constants.Slogan.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Slogan.minSize, Constants.Slogan.maxSize)
            }

            @NestedTest
            @DisplayName("targetAudience")
            inner class TargetAudienceValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "targetAudience"
                }

                @Test
                @DisplayName(
                    "size should be between ${Constants.TargetAudience.minSize} and " +
                        "${Constants.TargetAudience.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(Constants.TargetAudience.minSize, Constants.TargetAudience.maxSize)
            }

            @NestedTest
            @DisplayName("github")
            inner class GithubValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "github"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be URL`() = validationTester.isUrl()
            }
        }
    }

    @NestedTest
    @DisplayName("DELETE /projects/{projectId}")
    inner class DeleteProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
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
        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)

        private val newTitle = "New Title"
        private val newDescription = "New description of the project"
        private val newTeamMembers = mutableListOf<Long>()
        private val newHallOfFame = mutableListOf<Long>()
        private val newIsArchived = true
        private val newSlug = "new-slug"
        private val newSlogan = "new slogan"
        private val newTargetAudience = "new target audience"
        private val newGithub = "https://github.com/NIAEFEUP/nijobs-be"
        private val newLinks = mutableListOf<CustomWebsite>()
        private val newTimeline = mutableListOf<TimelineEvent>()

        val parameters = listOf(parameterWithName("id").description("ID of the project to update"))
        private lateinit var projectPart: MutableMap<String, Any>

        @BeforeEach
        fun addToRepositories() {
            projectPart = mutableMapOf(
                "title" to newTitle,
                "description" to newDescription,
                "teamMembersIds" to newTeamMembers,
                "hallOfFameIds" to newHallOfFame,
                "isArchived" to newIsArchived,
                "slug" to newSlug,
                "slogan" to newSlogan,
                "targetAudience" to newTargetAudience,
                "github" to newGithub,
                "links" to newLinks,
                "timeline" to newTimeline
            )

            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
            repository.save(testProject)
        }

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @Test
        fun `should update the project without image`() {
            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.hallOfFame.length()").value(newHallOfFame.size),
                    jsonPath("$.isArchived").value(newIsArchived),
                    jsonPath("$.slug").value(newSlug),
                    jsonPath("$.slogan").value(newSlogan),
                    jsonPath("$.targetAudience").value(newTargetAudience),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.links.length()").value(newLinks.size),
                    jsonPath("$.timeline.length()").value(newTimeline.size),
                    jsonPath("$.image").value(testProject.image)
                )
//                .andDocument(
//                    documentation,
//                    "Update projects",
//                    "Update a previously created project, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
            assertEquals(newSlug, updatedProject.slug)
            assertEquals(newSlogan, updatedProject.slogan)
            assertEquals(newTargetAudience, updatedProject.targetAudience)
            assertEquals(newGithub, updatedProject.github)
            assertEquals(newLinks, updatedProject.links)
            assertEquals(newTimeline, updatedProject.timeline)
            assertEquals(testProject.image, updatedProject.image)
        }

        @Test
        fun `should update the project with different hall of fame members`() {
            projectPart["hallOfFameIds"] = listOf(testAccount.id!!)

            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.hallOfFame.length()").value(1),
                    jsonPath("$.hallOfFame[0].id").value(testAccount.id!!)
                )
//                .andDocument(
//                    documentation,
//                    "Update projects",
//                    "Update a previously created project, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(1, updatedProject.hallOfFame.size)
            assertEquals(testAccount.id, updatedProject.hallOfFame[0].id)
        }

        @Test
        fun `should update the project with different team members`() {
            projectPart["teamMembersIds"] = listOf(testAccount2.id!!)

            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].id").value(testAccount2.id!!)
                )
//                .andDocument(
//                    documentation,
//                    "Update projects",
//                    "Update a previously created project, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(1, updatedProject.teamMembers.size)
            assertEquals(testAccount2.id, updatedProject.teamMembers[0].id)
        }

        @Test
        fun `should update the project with the same slug`() {
            projectPart["slug"] = testProject.slug!!
            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.isArchived").value(newIsArchived),
                    jsonPath("$.slug").value(testProject.slug),
                    jsonPath("$.slogan").value(newSlogan),
                    jsonPath("$.targetAudience").value(newTargetAudience),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.links.length()").value(newLinks.size),
                    jsonPath("$.timeline.length()").value(newTimeline.size)
                )
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.multipartBuilder("/projects/1234")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234")
                )
                .andDocumentErrorResponse(
                    documentation,
                    hasRequestPayload = true
                )
        }

        @Test
        fun `should fail if the slug already exists`() {
            val otherProject = Project(
                title = newTitle,
                description = newDescription,
                teamMembers = mutableListOf(),
                image = "image.png",
                slug = newSlug,
                slogan = newSlogan,
                targetAudience = newTargetAudience,
                github = "https://github.com/NIAEFEUP/website-niaefeup-frontend",
                links = mutableListOf(),
                timeline = mutableListOf()
            )
            repository.save(otherProject)

            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should update the project with image`() {
            val expectedImagePath = "${uploadConfigProperties.staticServe}/projects/$newTitle-$uuid.jpeg"

            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .addFile("image", "new-image.jpeg", contentType = MediaType.IMAGE_JPEG_VALUE)
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.isArchived").value(newIsArchived),
                    jsonPath("$.slug").value(newSlug),
                    jsonPath("$.slogan").value(newSlogan),
                    jsonPath("$.targetAudience").value(newTargetAudience),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.links.length()").value(newLinks.size),
                    jsonPath("$.timeline.length()").value(newTimeline.size),
                    jsonPath("$.image").value(expectedImagePath)
                )
//                .andDocument(
//                    documentation,
//                    "Update projects",
//                    "Update a previously created project, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
            assertEquals(newSlug, updatedProject.slug)
            assertEquals(newSlogan, updatedProject.slogan)
            assertEquals(newTargetAudience, updatedProject.targetAudience)
            assertEquals(newGithub, updatedProject.github)
            assertEquals(newLinks, updatedProject.links)
            assertEquals(newTimeline, updatedProject.timeline)
            assertEquals(expectedImagePath, updatedProject.image)
        }

        @Test
        fun `should fail to update project with invalid filename extension`() {
            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .addFile(name = "image", filename = "image.pdf")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("updateProjectById.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to update project with invalid filename media type`() {
            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .addPart("project", objectMapper.writeValueAsString(projectPart))
                .addFile(name = "image", contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("updateProjectById.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail when missing project part`() {
            mockMvc.multipartBuilder("/projects/${testProject.id}")
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("required"),
                    jsonPath("$.errors[0].param").value("project")
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.multipartBuilder("/projects/${testProject.id}")
                        .asPutMethod()
                        .addPart("project", objectMapper.writeValueAsString(params))
                        .perform()
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description,
                    "targetAudience" to testProject.targetAudience
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Title.minSize} and ${ActivityConstants.Title.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
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
                    "size should be between ${ActivityConstants.Description.minSize}" +
                        " and ${ActivityConstants.Description.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Description.minSize,
                        ActivityConstants.Description.maxSize
                    )
            }

            @NestedTest
            @DisplayName("slug")
            inner class SlugValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slug"
                }

                @Test
                @DisplayName(
                    "size should be between ${ActivityConstants.Slug.minSize} and ${ActivityConstants.Slug.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Slug.minSize, ActivityConstants.Slug.maxSize)
            }

            @NestedTest
            @DisplayName("slogan")
            inner class SloganValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slogan"
                }

                @Test
                @DisplayName("size should be between ${Constants.Slogan.minSize} and ${Constants.Slogan.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Slogan.minSize, Constants.Slogan.maxSize)
            }

            @NestedTest
            @DisplayName("targetAudience")
            inner class TargetAudienceValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "targetAudience"
                }

                @Test
                @DisplayName(
                    "size should be between ${Constants.TargetAudience.minSize} and " +
                        "${Constants.TargetAudience.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(Constants.TargetAudience.minSize, Constants.TargetAudience.maxSize)
            }

            @NestedTest
            @DisplayName("github")
            inner class GithubValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "github"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be URL`() = validationTester.isUrl()
            }

            @NestedTest
            @DisplayName("links")
            inner class LinksValidation {
                private val validationTester = ValidationTester(
                    req = { params: Map<String, Any?> ->
                        val projectPart = objectMapper.writeValueAsString(
                            mapOf(
                                "title" to testProject.title,
                                "description" to testProject.description,
                                "targetAudience" to testProject.targetAudience,
                                "links" to listOf<Any>(params)
                            )
                        )

                        mockMvc.multipartBuilder("/projects/${testProject.id}")
                            .addPart(
                                "project",
                                projectPart
                            )
                            .asPutMethod()
                            .perform()
                            .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                    },
                    requiredFields = mapOf(
                        "url" to "https://www.google.com"
                    )
                )

                @NestedTest
                @DisplayName("url")
                inner class UrlValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "url"
                    }

                    @Test
                    fun `should be required`() {
                        validationTester.parameterName = "url"
                        validationTester.isRequired()
                    }

                    @Test
                    fun `should not be empty`() {
                        validationTester.parameterName = "links[0].url"
                        validationTester.isNotEmpty()
                    }

                    @Test
                    fun `should be URL`() {
                        validationTester.parameterName = "links[0].url"
                        validationTester.isUrl()
                    }
                }

                @NestedTest
                @DisplayName("iconPath")
                inner class IconPathValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "iconPath"
                    }

                    @Test
                    fun `should be null or not blank`() {
                        validationTester.parameterName = "links[0].iconPath"
                        validationTester.isNullOrNotBlank()
                    }

                    @Test
                    fun `must be URL`() {
                        validationTester.parameterName = "links[0].iconPath"
                        validationTester.isUrl()
                    }
                }

                @NestedTest
                @DisplayName("label")
                inner class LabelValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "label"
                    }

                    @Test
                    fun `should be null or not blank`() {
                        validationTester.parameterName = "links[0].label"
                        validationTester.isNullOrNotBlank()
                    }
                }
            }

            @NestedTest
            @DisplayName("timeline")
            inner class TimelineValidation {
                private val validationTester = ValidationTester(
                    req = { params: Map<String, Any?> ->
                        val projectPart = objectMapper.writeValueAsString(
                            mapOf(
                                "title" to testProject.title,
                                "description" to testProject.description,
                                "targetAudience" to testProject.targetAudience,
                                "timeline" to listOf<Any>(params)
                            )
                        )

                        mockMvc.multipartBuilder("/projects/${testProject.id}")
                            .addPart(
                                "project",
                                projectPart
                            )
                            .asPutMethod()
                            .perform()
                            .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                    },
                    requiredFields = mapOf(
                        "date" to "22-07-2021",
                        "description" to "test description"
                    )
                )

                @NestedTest
                @DisplayName("date")
                inner class DateValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "date"
                    }

                    @Test
                    fun `should be required`() = validationTester.isRequired()
                }

                @NestedTest
                @DisplayName("description")
                inner class DescriptionValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "description"
                    }

                    @Test
                    fun `should be required`() {
                        validationTester.parameterName = "description"
                        validationTester.isRequired()
                    }

                    @Test
                    fun `should not be empty`() {
                        validationTester.parameterName = "timeline[0].description"
                        validationTester.isNotEmpty()
                    }
                }
            }
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/archive")
    inner class ArchiveProject {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
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
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived)
                )
                .andDocument(
                    documentation,
                    "Archive projects",
                    """
                        |This endpoint archives a project.
                        |This is useful to mark no longer maintained or complete projects.
                    """.trimMargin(),
                    urlParameters = parameters
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
            mutableListOf(),
            null,
            "cool-image.png",
            true,
            listOf("React", "TailwindCSS"),
            "Nice one",
            "students",
            "https://github.com/NIAEFEUP/website-niaefeup-frontend"
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
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived)
                )
                .andDocument(
                    documentation,
                    "Unarchive projects",
                    """
                        |This endpoint unarchives a project.
                        |This is useful to mark previously archived projects as active.
                    """.trimMargin(),
                    urlParameters = parameters
                )

            val unarchivedProject = repository.findById(project.id!!).get()
            assertEquals(newIsArchived, unarchivedProject.isArchived)
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/team/{accountId}")
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
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "test")
            )
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
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
                put("/projects/{projectId}/team/{accountId}", testProject.id, newAccount.id)
            )
                .andExpectAll(
                    status().isOk, content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(2),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].bio").value(testAccount.bio),
                    jsonPath("$.teamMembers[0].birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[0].linkedin").value(testAccount.linkedin),
                    jsonPath("$.teamMembers[0].github").value(testAccount.github),
                    jsonPath("$.teamMembers[0].websites.length()").value(1),
                    jsonPath("$.teamMembers[0].websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.teamMembers[0].websites[0].iconPath").value(testAccount.websites[0].iconPath),
                    jsonPath("$.teamMembers[1].name").value(newAccount.name),
                    jsonPath("$.teamMembers[1].email").value(newAccount.email),
                    jsonPath("$.teamMembers[1].bio").value(newAccount.bio),
                    jsonPath("$.teamMembers[1].birthDate").value(newAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[1].linkedin").value(newAccount.linkedin),
                    jsonPath("$.teamMembers[1].github").value(newAccount.github),
                    jsonPath("$.teamMembers[1].websites.length()").value(1),
                    jsonPath("$.teamMembers[1].websites[0].url").value(newAccount.websites[0].url),
                    jsonPath("$.teamMembers[1].websites[0].iconPath").value(newAccount.websites[0].iconPath)
                )
                .andDocument(
                    documentation,
                    "Add team member to Project",
                    "This operation adds a team member to a given project.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/projects/{projectId}/team/{accountId}", testProject.id, 1234))
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
    @DisplayName("DELETE /projects/{projectId}/team/{accountId}")
    inner class RemoveTeamMember {

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
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
            mockMvc.perform(delete("/projects/{projectId}/team/{accountId}", testProject.id, testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(0)
                )
                .andDocument(
                    documentation,
                    "Remove team member from Project",
                    "This operation removes a team member of a given project.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(delete("/projects/{projectId}/team/{accountId}", testProject.id, 1234))
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
    @DisplayName("PUT /projects/{idProject}/addHallOfFameMember/{idAccount}")
    inner class AddHallOfFameMember {
        private val newAccount = Account(
            "Another test Account",
            "test3_account@test.com",
            "test_password",
            "This is another test account too",
            TestUtils.createDate(2002, Calendar.JULY, 1),
            "https://test-photo.com",
            "https://linkedin.com",
            "https://github.com",
            listOf(
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "Test Website")
            )
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
            accountRepository.save(newAccount)
            repository.save(testProject)
        }

        private val parameters = listOf(
            parameterWithName("idProject").description(
                "ID of the project whose hall of fame the account will be added to"
            ),
            parameterWithName("idAccount").description("ID of the account to add")
        )

        @Test
        fun `should add account to project's hall of fame`() {
            mockMvc.perform(
                put("/projects/{idProject}/addHallOfFameMember/{idAccount}", testProject.id, newAccount.id)
            )
                .andExpectAll(
                    status().isOk, content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.hallOfFame.length()").value(2),
                    jsonPath("$.hallOfFame[0].name").value(testAccount2.name),
                    jsonPath("$.hallOfFame[0].email").value(testAccount2.email),
                    jsonPath("$.hallOfFame[0].bio").value(testAccount2.bio),
                    jsonPath("$.hallOfFame[0].birthDate").value(testAccount2.birthDate.toJson()),
                    jsonPath("$.hallOfFame[0].linkedin").value(testAccount2.linkedin),
                    jsonPath("$.hallOfFame[0].github").value(testAccount2.github),
                    jsonPath("$.hallOfFame[0].websites.length()").value(1),
                    jsonPath("$.hallOfFame[0].websites[0].url").value(testAccount2.websites[0].url),
                    jsonPath("$.hallOfFame[0].websites[0].iconPath").value(testAccount2.websites[0].iconPath),
                    jsonPath("$.hallOfFame[1].name").value(newAccount.name),
                    jsonPath("$.hallOfFame[1].email").value(newAccount.email),
                    jsonPath("$.hallOfFame[1].bio").value(newAccount.bio),
                    jsonPath("$.hallOfFame[1].birthDate").value(newAccount.birthDate.toJson()),
                    jsonPath("$.hallOfFame[1].linkedin").value(newAccount.linkedin),
                    jsonPath("$.hallOfFame[1].github").value(newAccount.github),
                    jsonPath("$.hallOfFame[1].websites.length()").value(1),
                    jsonPath("$.hallOfFame[1].websites[0].url").value(newAccount.websites[0].url),
                    jsonPath("$.hallOfFame[1].websites[0].iconPath").value(newAccount.websites[0].iconPath)
                )
                .andDocument(
                    documentation,
                    "Add account to the Project's Hall Of Fame",
                    "This operation adds an account to a given project's hall of fame.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.perform(put("/projects/{idProject}/addHallOfFameMember/{idAccount}", testProject.id, 1234))
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
    @DisplayName("PUT /projects/{idProject}/removeHallOfFameMember/{idAccount}")
    inner class RemoveHallOfFameMember {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
            repository.save(testProject)
        }

        private val parameters = listOf(
            parameterWithName("idProject").description(
                "ID of the project whose hall of fame the account will be removed from"
            ),
            parameterWithName("idAccount").description("ID of the account to remove")
        )

        @Test
        fun `should remove an account from project's hall of fame`() {
            mockMvc.perform(
                put("/projects/{idProject}/removeHallOfFameMember/{idAccount}", testProject.id, testAccount2.id)
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.hallOfFame.length()").value(0)
                )
                .andDocument(
                    documentation,
                    "Remove account from Project's Hall of Fame",
                    "This operation removes an account from a given project's hall of fame.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.perform(put("/projects/{idProject}/removeHallOfFameMember/{idAccount}", testProject.id, 1234))
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
