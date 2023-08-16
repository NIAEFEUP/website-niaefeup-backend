package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import java.util.Calendar
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadPermissions
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadRoles
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse

@ControllerTest
@Transactional
internal class RoleControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val roleRepository: RoleRepository,
    val accountRepository: AccountRepository,
    val generationRepository: GenerationRepository,
    val projectRepository: ProjectRepository
) {
    val testGeneration = Generation(
        "22-23"
    )

    val lastYearGeneration = Generation(
        "21-22"
    )

    val documentationRoles = PayloadRoles()

    val documentationPermissions = PayloadPermissions()

    val testRole = Role(
        "TestRole",
        Permissions(listOf(Permission.CREATE_ACTIVITY)),
        false
    )
    val otherTestRole = Role(
        "Coordenador de Projetos",
        Permissions(listOf(Permission.EDIT_ACCOUNT)),
        false
    )

    val project = Project(
        "UNI",
        "Melhor app",
        image = "image.png",
        targetAudience = "Estudantes"
    )

    val testAccount = Account(
        "TestAccount",
        "something@pog.com",
        "test_password",
        "This is a test account",
        TestUtils.createDate(2003, Calendar.NOVEMBER, 13),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "Website pessoal")
        ),
        mutableListOf()
    )

    @NestedTest
    @DisplayName("GET /roles")
    inner class GetAllRoles {

        private val roles = listOf(
            testRole,
            Role(
                "El Presidant",
                Permissions(listOf(Permission.SUPERUSER)),
                true
            )
        )

        @BeforeEach
        fun addRoles() {
            for (role in roles) {
                roleRepository.save(role)
            }
        }

        @Test
        fun `should return all roles`() {
            mockMvc.perform(get("/roles")).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().json(objectMapper.writeValueAsString(roles))
            ).andDocument(
                documentationRoles.getModelDocumentationArray(),
                "Returns all existing roles",
                "This endpoint returns all existing roles in the database for ease of use."
            )
        }
    }

    @NestedTest
    @DisplayName("GET /roles/{id}")
    inner class GetSpecificRole {

        private val parameters = listOf(parameterWithName("id").description("ID of the role to retrieve"))

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should return testRole when provided by its id`() {
            mockMvc.perform(get("/roles/{id}", testRole.id)).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().json(objectMapper.writeValueAsString(testRole))
            ).andDocument(
                documentationRoles,
                "Returns a specific role",
                "Returns a summary brief of a specific role, which makes getting one role way more efficient.",
                urlParameters = parameters
            )
        }

        @Test
        fun `should return error on invalid roleID`() {
            mockMvc.perform(get("/roles/{id}", 4020)).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors.[0].message").value("role not found with id 4020")
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
        }
    }

    @NestedTest
    @DisplayName("POST /roles")
    inner class CreateNewRole {

        @BeforeEach
        fun addRole() {
            testRole.generation = testGeneration
            generationRepository.save(testGeneration)
            generationRepository.save(lastYearGeneration)
            roleRepository.save(testRole)
            TestUtils.startNewTransaction()
        }

        @Test
        fun `should add new role`() {
            mockMvc.perform(
                post("/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to otherTestRole.name,
                                "permissions" to otherTestRole.permissions.map { it.bit }.toList(),
                                "isSection" to otherTestRole.isSection,
                                "associatedActivities" to otherTestRole.associatedActivities,
                                "accounts" to otherTestRole.accounts
                            )
                        )
                    )
            ).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.name").value(otherTestRole.name),
                jsonPath("$.id").value(2), // this must be hardcoded
                jsonPath("$.permissions.length()").value(otherTestRole.permissions.size),
                jsonPath("$.isSection").value(otherTestRole.isSection),
                jsonPath("$.associatedActivities").value(otherTestRole.associatedActivities)
            ).andDocument(
                documentationRoles,
                "This creates a role, returning the complete role information",
                "It will only create the role if it no other role with the same name exists in that generation"

            )
            assert(roleRepository.count().toInt() == 2)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 2)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles[1].id!!.compareTo(2) == 0)
        }

        @Test
        fun `shouldn't add role with same name`() {
            mockMvc.perform(
                post("/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to testRole.name,
                                "permissions" to otherTestRole.permissions.map { it.bit }.toList(),
                                "isSection" to testRole.isSection,
                                "associatedActivities" to testRole.associatedActivities,
                                "accounts" to testRole.accounts
                            )
                        )
                    )
            ).andExpectAll(
                status().isUnprocessableEntity,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1)
            )
                .andDocumentErrorResponse(documentationRoles, hasRequestPayload = true)
            TestUtils.startNewTransaction(rollback = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!) != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size != 2)
        }

        @Test
        fun `should add a role with an existing name but on a different generation`() {
            mockMvc.perform(
                post("/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to testRole.name,
                                "permissions" to testRole.permissions.map { it.bit }.toList(),
                                "isSection" to testRole.isSection,
                                "associatedActivities" to testRole.associatedActivities,
                                "accounts" to testRole.accounts,
                                "generationId" to lastYearGeneration.id
                            )
                        )
                    )
            ).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.name").value("TestRole"),
                jsonPath("$.id").value(2), // this must be hardcoded
                jsonPath("$.permissions.length()").value(1),
                jsonPath("$.isSection").value(false)
            ).andDocument(
                documentationRoles,
                "This creates a role on a specified generation, returning the complete role information",
                "It will only create the role if it no other role with the same name exists in that generation",
                hasRequestPayload = true
            )
            TestUtils.startNewTransaction()
            assert(generationRepository.findById(lastYearGeneration.id!!).orElseThrow().roles.size == 1)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 1)
        }
    }

    @NestedTest
    @DisplayName("DELETE /roles/{id}")
    inner class DeleteRole {
        private val parameters = listOf(parameterWithName("id").description("ID of the role to delete"))

        private lateinit var generation1: Generation
        private lateinit var role: Role

        @BeforeEach
        fun addRole() {
            generation1 = Generation("22-23")
            generationRepository.save(generation1)
            role = Role("test-role-1", Permissions(), true)
            role.generation = generation1
            roleRepository.save(role)
            TestUtils.startNewTransaction()
        }

        @AfterEach
        fun removeRole() {
            generationRepository.deleteAll()
        }

        @Test
        fun `should remove role with correct id`() {
            val id: Long = role.id!!
            mockMvc.perform(delete("/roles/{id}", role.id)).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationRoles,
                "Removes the role by its id",
                "The id must exist in order to remove it correctly",
                urlParameters = parameters
            )
            TestUtils.startNewTransaction()
            assert(roleRepository.findByIdOrNull(id) == null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 0)
        }

        @Test
        fun `should not remove role if id does not exist`() {
            mockMvc.perform(delete("/roles/{id}", 1234)).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors.[0].message").value("role not found with id 1234")
            )
                .andDocumentErrorResponse(
                    documentationRoles,
                    urlParameters = parameters
                )
        }
    }

    @NestedTest
    @DisplayName("POST /roles/{id}/permissions")
    inner class GrantPermissionToRole {
        private val parameters = listOf(parameterWithName("id").description("ID of the role to grant permissions to"))

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should grant permission to role that exists`() {
            mockMvc.perform(
                post("/roles/{id}/permissions", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "permissions" to Permissions(listOf(Permission.SUPERUSER))
                            )
                        )
                    )
            ).andExpectAll(
                status().isOk
            )
                .andDocumentEmptyObjectResponse(
                    documentationPermissions,
                    "Adds a set of permissions to a role by ID",
                    "This doesn't check if the permission is already added, the role ID must be valid",

                    urlParameters = parameters
                )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should not grant permission to role that does not exists`() {
            mockMvc.perform(
                post("/roles/{id}/permissions", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "permissions" to Permissions(listOf(Permission.SUPERUSER))
                            )
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                urlParameters = parameters
            )
        }
    }

    @NestedTest
    @DisplayName("DELETE /roles/{id}/permissions")
    inner class RevokePermissionFromRole {
        private val parameters = listOf(
            parameterWithName("id").description("ID of the role to revoke permissions from")
        )

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should revoke permission from role that exists`() {
            mockMvc.perform(
                delete("/roles/{id}/permissions", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "permissions" to Permissions(listOf(Permission.SUPERUSER))
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isOk
                ).andDocumentEmptyObjectResponse(
                    documentationPermissions,
                    "Revokes permission by role ID",
                    "Revokes permissions, it doesn't check if the role contains them.",

                    urlParameters = parameters
                )
            assert(!roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should not revoke permission from role that does not exist`() {
            mockMvc.perform(
                delete("/roles/{id}/permissions", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "permissions" to Permissions(listOf(Permission.SUPERUSER))
                            )
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters
            )
        }
    }

    @NestedTest
    @DisplayName("POST /roles/{id}/users")
    inner class AddUserToRole {
        private val parameters = listOf(parameterWithName("id").description("ID of the role to add an user"))

        @BeforeEach
        fun addRoleAndUser() {
            roleRepository.save(testRole)
            accountRepository.save(testAccount)
            TestUtils.startNewTransaction()
        }

        @Test
        fun `should add an existing account to an existing role`() {
            mockMvc.perform(
                post("/roles/{id}/users", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationRoles,
                "Add an account to a role by its IDs",
                "It's an idempotent endpoint so it will return HTTP code 200 even if the user is already " +
                    "in the role",
                hasRequestPayload = true,
                urlParameters = parameters
            )
            TestUtils.startNewTransaction()
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
            assert(accountRepository.findByIdOrNull(testAccount.id!!)!!.roles.size != 0)
        }

        @Test
        fun `should not add an non existing account to an existing role`() {
            mockMvc.perform(
                post("/roles/{id}/users", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an non existing account to an non existing role`() {
            mockMvc.perform(
                post("/roles/{id}/users", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an existing account to an non existing role`() {
            mockMvc.perform(
                post("/roles/{id}/users", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }
    }

    @NestedTest
    @DisplayName("DELETE /roles/{id}/users")
    inner class RemoveUserFromRole {
        private val parameters = listOf(parameterWithName("id").description("ID of the role to remove an user from"))

        @BeforeEach
        fun addRoleAndUser() {
            roleRepository.save(testRole)
            accountRepository.save(testAccount)
            testRole.accounts.add(testAccount)
            roleRepository.save(testRole)
        }

        @AfterEach
        fun removeAssociations() {
            if (testRole.accounts.size != 0) testRole.accounts.clear()
        }

        @Test
        fun `should remove an existing account from an existing role`() {
            mockMvc.perform(
                delete("/roles/{id}/users", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationRoles,
                "Removes the account from the role by ID",
                "It only works if the user has the role and it exists",
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not remove a non existing account from an existing role`() {
            mockMvc.perform(
                delete("/roles/{id}/users", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an non existing account to an non existing role`() {
            mockMvc.perform(
                delete("/roles/{id}/users", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an existing account to an non existing role`() {
            mockMvc.perform(
                delete("/roles/{id}/users", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isNotFound
            ).andDocumentErrorResponse(
                documentationRoles,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }
    }

    @NestedTest
    @DisplayName("POST /roles/{id}/activities/{activityId}/permissions")
    inner class AddPermissionToRoleActivity {
        private val parameters = listOf(
            parameterWithName("id").description("ID of the role"),
            parameterWithName("activityId").description("ID of the activity")
        )

        @BeforeEach
        fun addAll() {
            roleRepository.save(testRole)
            projectRepository.save(project)
            TestUtils.startNewTransaction()
        }

        @Test
        fun `should add permission to role activity and create PerActivityRole`() {
            mockMvc.perform(
                post("/roles/{id}/activities/{activityId}/permissions", testRole.id, project.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )
            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationPermissions,
                "Adds an permission to a role activity",
                "It will create a PerRoleActivity if it doesn't exist",
                hasRequestPayload = true,
                urlParameters = parameters
            )
            TestUtils.startNewTransaction()
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 1)
            assert(
                roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `shouldn't add permission to role activity if roleId is invalid`() {
            mockMvc.perform(
                post("/roles/{id}/activities/{activityId}/permissions", 1234, project.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters

            )
            TestUtils.startNewTransaction(rollback = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }

        @Test
        fun `shouldn't add permission to role activity if activityId is invalid`() {
            mockMvc.perform(
                post("/roles/{id}/activities/{activityId}/permissions", testRole.id, 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters

            )
            TestUtils.startNewTransaction(rollback = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }

        @Test
        fun `shouldn't add permission to role activity if activityId is invalid and roleId is invalid`() {
            mockMvc.perform(
                post("/roles/{id}/activities/{activityId}/permissions", 1234, 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            TestUtils.startNewTransaction(rollback = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }
    }

    @NestedTest
    @DisplayName("DELETE /roles/{id}/activities/{activityId}/permissions")
    inner class RemovePermissionFromPerRoleActivity {
        private val parameters = listOf(
            parameterWithName("id").description("ID of the role"),
            parameterWithName("activityId").description("ID of the activity")
        )

        private lateinit var project: Project

        @BeforeEach
        fun addAll() {
            project = Project("test project", "test", image = "image.png", targetAudience = "estudantes")
            roleRepository.save(testRole)
            projectRepository.save(project)
            val perActivityRole = PerActivityRole(Permissions(listOf(Permission.EDIT_ACTIVITY)))
            perActivityRole.activity = project
            perActivityRole.role = testRole
            project.associatedRoles.add(perActivityRole)
            projectRepository.save(project)
            roleRepository.save(testRole)
        }

        @Test
        fun `should remove an existing role activity permission`() {
            mockMvc.perform(
                delete("/roles/{id}/activities/{activityId}/permissions", testRole.id, project.id)
                    .contentType(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationPermissions,
                "Removes an PerRoleActivity permission by ID",
                "It will not create a PerRoleActivity if it doesn't exist, it will simply return, " +
                    "doesn't check if the permissions are already revoked...",
                hasRequestPayload = true,
                urlParameters = parameters

            )
            assert(projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles.size == 1)
            assert(
                !projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role activity permission on a non existing role`() {
            mockMvc.perform(
                delete("/roles/{id}/activities/{activityId}/permissions", 1234, project.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters

            )
            assert(projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role activity permission on a non existing activity`() {
            mockMvc.perform(
                delete("/roles/{id}/activities/{activityId}/permissions", testRole.id, 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters
            )
            assert(projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role permission when neither the activity and role don't exist`() {
            mockMvc.perform(
                delete("/roles/{id}/activities/{activityId}/permissions", 1234, 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )

                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(
                documentationPermissions,
                hasRequestPayload = true,
                urlParameters = parameters

            )
            assert(projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(project.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }
    }

    @NestedTest
    @DisplayName("PUT /roles/{id}")
    inner class UpdateRoleTest {

        @BeforeEach
        fun addAll() {
            generationRepository.save(testGeneration)
            testRole.generation = testGeneration
            otherTestRole.generation = testGeneration
            roleRepository.save(testRole)
            roleRepository.save(otherTestRole)
            TestUtils.startNewTransaction()
        }

        @Test
        fun `should correctly update role name and isSection`() {
            mockMvc.perform(
                put("/roles/{id}", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to "Membro",
                                "isSection" to true
                            )
                        )
                    )
            )
                .andExpectAll(status().isOk)
                .andDocument(
                    documentationRoles,
                    "Updates role information by ID",
                    "Updates some role information (eg: name and isSection). " +
                        "It will throw an error if the desired name already exists on role generation " +
                        "or if role doesn't exist.",
                    hasRequestPayload = true
                )
            TestUtils.startNewTransaction()
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.name == "Membro")
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.isSection)
        }

        @Test
        fun `should not update role name if it already exists in generation`() {
            mockMvc.perform(
                put("/roles/{id}", testRole.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to otherTestRole.name,
                                "isSection" to true
                            )
                        )
                    )
            )
                .andExpectAll(status().isUnprocessableEntity)
                .andDocumentErrorResponse(documentationRoles)
            TestUtils.startNewTransaction(rollback = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.name == "TestRole")
            assert(!roleRepository.findByIdOrNull(testRole.id!!)!!.isSection)
        }

        @Test
        fun `should not update role name if role doesn't exist`() {
            mockMvc.perform(
                put("/roles/{id}", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to otherTestRole.name,
                                "isSection" to true
                            )
                        )
                    )
            )
                .andExpectAll(status().isNotFound)
        }
    }
}
