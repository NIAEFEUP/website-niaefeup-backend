package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.EmptyObjectSchema
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

@ControllerTest
@AutoConfigureRestDocs
internal class ProjectControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: ProjectRepository,
) {
    val testProject = Project(
        "Awesome project",
        "this is a test project",
        false,
        listOf("Java", "Kotlin", "Spring"),
    )

    private val projectFields = listOf<FieldDescriptor>(
        fieldWithPath("title").type(JsonFieldType.STRING).description("Project title"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("Project description"),
        fieldWithPath("isArchived").type(JsonFieldType.BOOLEAN).description("Whether this project is being actively maintained"),
        fieldWithPath("technologies").type(JsonFieldType.ARRAY).description("Array of technologies used in the project").optional(),
        fieldWithPath("associatedRoles[]").type(JsonFieldType.ARRAY).description("An activity that aggregates members with different roles").optional(),
        fieldWithPath("associatedRoles[].*.permissions").type(JsonFieldType.OBJECT).description("Permissions of someone with a given role for this activity").optional(),
        fieldWithPath("associatedRoles[].*.id").type(JsonFieldType.NUMBER).description("Id of the role/activity association").optional(),
    )
    private val projectPayloadSchema = PayloadSchema("project", projectFields)
    private val responseOnlyProjectFields = listOf<FieldDescriptor>(fieldWithPath("id").type(JsonFieldType.NUMBER).description("Project ID"))

    @NestedTest
    @DisplayName("GET /projects")
    inner class GetAllProjects {
        private val testProjects = listOf(
            testProject,
            Project(
                "NIJobs",
                "Job platform for students",
                false,
                listOf("ExpressJS", "React"),
            ),
        )

        @BeforeEach
        fun addProjects() {
            for (project in testProjects) repository.save(project)
        }

        @Test
        fun `should return all projects`() {
            mockMvc.perform(get("/projects"))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    content().json(objectMapper.writeValueAsString(testProjects)),
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
                                        """.trimIndent(),
                                    )
                                    .responseSchema(projectPayloadSchema.Response().arraySchema())
                                    .responseFields(projectPayloadSchema.Response().arrayDocumentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }
    }

    @NestedTest
    @DisplayName("GET /projects/{projectId}")
    inner class GetProject {
        @BeforeEach
        fun addProject() {
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
                                        """.trimIndent(),
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to retrieve"))
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the project does not exist`() {
            mockMvc.perform(get("/projects/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(parameterWithName("id").description("ID of the project to retrieve"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }
    }

    @NestedTest
    @DisplayName("POST /projects/new")
    inner class CreateProject {
        @Test
        fun `should create a new project`() {
            mockMvc.perform(
                post("/projects/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testProject)),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testProject.title),
                    jsonPath("$.description").value(testProject.description),
                    jsonPath("$.technologies.length()").value(testProject.technologies.size),
                    jsonPath("$.technologies[0]").value(testProject.technologies[0]),
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
                                        """.trimIndent(),
                                    )
                                    .requestSchema(projectPayloadSchema.Request().schema())
                                    .requestFields(projectPayloadSchema.Request().documentedFields())
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
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
                        post("/projects/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "events/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Projects")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description,
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

    @NestedTest
    @DisplayName("DELETE /projects/{projectId}")
    inner class DeleteProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should delete the project`() {
            mockMvc.perform(delete("/projects/{id}", testProject.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty,
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
                                        """.trimIndent(),
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to delete"))
                                    .responseSchema(EmptyObjectSchema().Response().schema())
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
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
                    jsonPath("$.errors[0].message").value("project not found with id 1234"),
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
                                    .build(),
                            ),
                        ),
                    ),
                )
        }
    }

    @NestedTest
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
            val newIsArchived = true

            mockMvc.perform(
                put("/projects/{id}", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "isArchived" to newIsArchived,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.isArchived").value(newIsArchived),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Update projects")
                                    .description(
                                        """Update previously created projects, using their ID.""",
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to update"))
                                    .requestSchema(projectPayloadSchema.Request().schema())
                                    .requestFields(projectPayloadSchema.Request().documentedFields())
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
                )

            val updatedProject = repository.findById(testProject.id!!).get()
            assertEquals(newTitle, updatedProject.title)
            assertEquals(newDescription, updatedProject.description)
            assertEquals(newIsArchived, updatedProject.isArchived)
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
                                "description" to "New description of the project",
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("project not found with id 1234"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .pathParameters(parameterWithName("id").description("ID of the project to update"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Projects")
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
                        put("/projects/{id}", testProject.id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            MockMvcRestDocumentationWrapper.document(
                                "projects/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                            .pathParameters(parameterWithName("id").description("ID of the project to update"))
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Projects")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "title" to testProject.title,
                    "description" to testProject.description,
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

    @NestedTest
    @DisplayName("PUT /projects/{projectId}/archive")
    inner class ArchiveProject {
        @BeforeEach
        fun addProject() {
            repository.save(testProject)
        }

        @Test
        fun `should archive the project`() {
            val newIsArchived = true

            mockMvc.perform(
                put("/projects/{id}/archive", testProject.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString("isArchived" to newIsArchived)),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Archive projects")
                                    .description(
                                        """This endpoint updates projects as archived. This is useful to mark no longer maintained or complete projects of the Nucleus.""",
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to archive"))
                                    .requestFields(
                                        fieldWithPath("first").type(JsonFieldType.STRING).description("String with property name (\"isArchived\")"),
                                        fieldWithPath("second").type(JsonFieldType.BOOLEAN).description("Whether the project is archived"),
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
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
            true,
            listOf("React", "TailwindCSS"),
        )

        @BeforeEach
        fun addProject() {
            repository.save(project)
        }

        @Test
        fun `should unarchive the project`() {
            val newIsArchived = false

            mockMvc.perform(
                put("/projects/{id}/unarchive", project.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString("isArchived" to newIsArchived)),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.isArchived").value(newIsArchived),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "projects/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            ResourceDocumentation.resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Unarchive projects")
                                    .description(
                                        """This endpoint updates projects as unarchived. This is useful to mark previously unarchived projects as active.""",
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the project to unarchive"))
                                    .requestSchema(Schema("project-archive-request"))
                                    .requestFields(
                                        fieldWithPath("first").type(JsonFieldType.STRING).description("String with property name (\"isArchived\")"),
                                        fieldWithPath("second").type(JsonFieldType.BOOLEAN).description("Whether the project is archived"),
                                    )
                                    .responseSchema(projectPayloadSchema.Response().schema())
                                    .responseFields(projectPayloadSchema.Response().documentedFields(responseOnlyProjectFields))
                                    .tag("Projects")
                                    .build(),
                            ),
                        ),
                    ),
                )

            val unarchivedProject = repository.findById(project.id!!).get()
            assertEquals(newIsArchived, unarchivedProject.isArchived)
        }
    }
}
