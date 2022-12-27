package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.EndpointTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import java.util.*
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

@ControllerTest
internal class ProjectControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: ProjectRepository,
    val accountRepository: AccountRepository
) {
    val testProject = Project(
        "Awesome project",
        "this is a test project",
        mutableListOf(
            Account(
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
        ),
        false,
        listOf("Java", "Kotlin", "Spring")
    )

    @EndpointTest
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

        @BeforeAll
        fun addProjects() {
            for (project in testProjects) repository.save(project)
        }

        @Test
        fun `should return all projects`() {
            mockMvc.get("/projects").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(testProjects)) }
            }
        }
    }

    @EndpointTest
    @DisplayName("GET /projects/{projectId}")
    inner class GetProject {
        @BeforeAll
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should return the project`() {
            mockMvc.get("/projects/${testProject.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.title") { value(testProject.title) }
                jsonPath("$.description") { value(testProject.description) }
                jsonPath("$.technologies.length()") { value(testProject.technologies.size) }
                jsonPath("$.technologies[0]") { value(testProject.technologies[0]) }
            }
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.get("/projects/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("project not found with id 1234") }
            }
        }
    }

    @EndpointTest
    @DisplayName("POST /projects/new")
    inner class CreateProject {
        @Test
        fun `should create a new project`() {
            mockMvc.post("/projects/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testProject)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testProject.title) }
                    jsonPath("$.description") { value(testProject.description) }
                    jsonPath("$.technologies.length()") { value(testProject.technologies.size) }
                    jsonPath("$.technologies[0]") { value(testProject.technologies[0]) }
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.post("/projects/new") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
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
                @DisplayName("size should be between ${Constants.Description.minSize} and ${Constants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
            }
        }
    }

    @EndpointTest
    @DisplayName("DELETE /projects/{projectId}")
    inner class DeleteProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should delete the project`() {
            mockMvc.delete("/projects/${testProject.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$") { isEmpty() }
            }

            assert(repository.findById(testProject.id!!).isEmpty)
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.delete("/projects/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("project not found with id 1234") }
            }
        }
    }

    @EndpointTest
    @DisplayName("PUT /projects/{projectId}")
    inner class UpdateProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should update the project`() {
            val newTitle = "New Title"
            val newDescription = "New description of the project"
            val newTeamMembers = mutableListOf<Account>()
            val newIsArchived = true

            mockMvc.put("/projects/${testProject.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "description" to newDescription,
                        "teamMembers" to newTeamMembers,
                        "isArchived" to newIsArchived
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(newTitle) }
                    jsonPath("$.description") { value(newDescription) }
                    jsonPath("$.teamMembers") { value(newTeamMembers) }
                    jsonPath("$.isArchived") { value(newIsArchived) }
                }

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newTeamMembers, updatedProject.teamMembers)
            assertEquals(newIsArchived, updatedProject.isArchived)
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.put("/projects/1234") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to "New Title",
                        "description" to "New description of the project"
                    )
                )
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("project not found with id 1234") }
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.put("/projects/${testProject.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
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
                @DisplayName("size should be between ${Constants.Description.minSize} and ${Constants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
            }
        }
    }

    @EndpointTest
    @DisplayName("PUT /projects/{projectId}/archive")
    inner class ArchiveProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should archive the project`() {
            val newIsArchived = true

            mockMvc.put("/projects/${testProject.id}/archive") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString("isArchived" to newIsArchived)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.isArchived") { value(newIsArchived) }
                }

            val archivedProject = repository.findById(testProject.id!!).get()
            assertEquals(newIsArchived, archivedProject.isArchived)
        }
    }

    @EndpointTest
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
        fun addProject() {
            repository.save(project)
        }

        @Test
        fun `should unarchive the project`() {
            val newIsArchived = false

            mockMvc.put("/projects/${project.id}/unarchive") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString("isArchived" to newIsArchived)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.isArchived") { value(newIsArchived) }
                }

            val unarchivedProject = repository.findById(project.id!!).get()
            assertEquals(newIsArchived, unarchivedProject.isArchived)
        }
    }

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/addTeamMember/{accountId}")
    inner class AddTeamMember {

        val testAccount = Account(
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
            repository.save(testProject)
        }

        @Test
        fun `should add a team member`() {
            val testAccount = Account(
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

            val newTeamMembers = mutableListOf(testProject.teamMembers[0], testAccount)

            mockMvc.put("/projects/${testProject.id}/addTeamMember/${testAccount.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString("teamMembers" to newTeamMembers)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.teamMembers") { value(newTeamMembers) }
                }

            val alteredProject = repository.findById(testProject.id!!).get()
            assertEquals(newTeamMembers, alteredProject.teamMembers)
        }
    }

    @DisplayName("PUT /projects/{projectId}/addTeamMember/{accountId}")
    inner class RemoveTeamMember {
        val testAccount = Account(
            "Another test Account",
            "test3_account@test.com",
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
            repository.save(testProject)
        }

        @Test
        fun `should remove a team member`() {
            val newTeamMembers = mutableListOf<Account>()

            mockMvc.put("/projects/${testProject.id}/removeTeamMember/${testProject.teamMembers[0].id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString("teamMembers" to newTeamMembers)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.teamMembers") { value(newTeamMembers) }
                }

            val alteredProject = repository.findById(testProject.id!!).get()
            assertEquals(newTeamMembers, alteredProject.teamMembers)
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.put("/projects/${testProject.id}/removeTeamMember/1234") {
                contentType = MediaType.APPLICATION_JSON
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("account not found with id 1234") }
                }
        }
    }
}
