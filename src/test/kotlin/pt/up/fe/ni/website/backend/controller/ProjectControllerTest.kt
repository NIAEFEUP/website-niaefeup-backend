package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
internal class ProjectControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: ProjectRepository
) {
    val testProject = Project(
        "Awesome project",
        "this is a test project"
    )

    @Nested
    @DisplayName("GET /projects")
    inner class GetAllProjects {
        private val testProjects = listOf(
            testProject,
            Project(
                "NIJobs",
                "Job platform for students"
            )
        )

        @BeforeEach
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

    @Nested
    @DisplayName("GET /projects/{projectId}")
    inner class GetProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should return the project`() {
            mockMvc.get("/projects/${testProject.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.name") { value(testProject.name) }
                jsonPath("$.description") { value(testProject.description) }
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

    @Nested
    @DisplayName("POST /projects/new")
    inner class CreateProject {
        @Test
        fun `should create a new project`() {
            mockMvc.post("/projects/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testProject)
            }
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.name") { value(testProject.name) }
                    jsonPath("$.description") { value(testProject.description) }
                }
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any> ->
                    mockMvc.post("/projects/new") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "name" to testProject.name,
                    "description" to testProject.description
                )
            )

            @Nested
            @DisplayName("name")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class NameValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "name"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Name.minSize} and ${Constants.Name.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Name.minSize, Constants.Name.maxSize)
            }

            @Nested
            @DisplayName("description")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Nested
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

    @Nested
    @DisplayName("PUT /projects/{projectId}")
    inner class UpdateProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should update the project`() {
            val newName = "New Name"
            val newDescription = "New description of the project"
            val newIsArchived = true

            mockMvc.put("/projects/${testProject.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "name" to newName,
                        "description" to newDescription,
                        "isArchived" to newIsArchived
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.name") { value(newName) }
                    jsonPath("$.description") { value(newDescription) }
                    jsonPath("$.isArchived") { value(newIsArchived) }
                }

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newName, updatedProject.name)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.put("/projects/1234") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "name" to "New Name",
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

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any> ->
                    mockMvc.put("/projects/${testProject.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "name" to testProject.name,
                    "description" to testProject.description
                )
            )

            @Nested
            @DisplayName("name")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class NameValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "name"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Name.minSize} and ${Constants.Name.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Name.minSize, Constants.Name.maxSize)
            }

            @Nested
            @DisplayName("description")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
}
