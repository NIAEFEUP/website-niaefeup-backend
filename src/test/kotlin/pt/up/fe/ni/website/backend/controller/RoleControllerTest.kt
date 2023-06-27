package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import java.util.*
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
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.function.RequestPredicates.contentType
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
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAccount
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadPermissions
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadRoles
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class AccountRoleDocumentation : ModelDocumentation(
    Tag.ROLES.name.lowercase() + "-accounts",
    Tag.ROLES,
    mutableListOf(
        DocumentedJSONField("[]", "Array that only contains ONE account id", JsonFieldType.ARRAY)
    )

)

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

    val documentationRoles = PayloadRoles()

    val documentationAccount = PayloadAccount()

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

    val testProject = Project(
        "UNI",
        "Melhor app"
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        ),
        mutableListOf()
    )

    @NestedTest
    @DisplayName("GET /roles")
    inner class GetAllRoles {

        val roles = listOf(
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
        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should return testRole when provided by its id`() {
            mockMvc.perform(get("/roles/${testRole.id}")).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().json(objectMapper.writeValueAsString(testRole))
            ).andDocument(
                documentationRoles,
                "Returns a specific role",
                "Returns a summary brief of a specific role, which makes getting one role way more efficient."
            )
        }

        @Test
        fun `should return error on invalid roleID`() {
            mockMvc.perform(get("/roles/4020")).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors.[0].message").value("role not found with id 4020")
            ).andDocumentErrorResponse(documentationRoles, hasRequestPayload = true)
        }
    }

    @NestedTest
    inner class CreateNewRole {

        @BeforeEach
        fun addRole() {
            generationRepository.save(testGeneration)
            roleRepository.save(testRole)
            testGeneration.roles.add(testRole)
            generationRepository.save(testGeneration)
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
            assert(generationRepository.findFirstByOrderBySchoolYearDesc() != null)
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
                status().isBadRequest,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
            )
                .andDocumentErrorResponse(documentationRoles, hasRequestPayload = true)
            assert(roleRepository.findByName(testRole.name) != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 2)
        }
    }

    @NestedTest
    inner class DeleteRole {
        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
            generationRepository.save(testGeneration)
            generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.add(testRole)
        }

        @AfterEach
        fun removeRole(){
            roleRepository.delete(testRole);
            testGeneration.roles.remove(testRole)
        }

        @Test
        fun `should remove role with correct id`() {
            mockMvc.perform(delete("/roles/${testRole.id}")).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationRoles,
                "Removes the role by its id",
                "The id must exist in order to remove it correctly"
            )
            assert(roleRepository.findByName(testRole.name) == null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc() != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 0)
        }

        @Test
        @Transactional
        fun `should not remove role if id does not exist`() {
            mockMvc.perform(delete("/roles/1234")).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors.[0].message").value("role not found with id 1234")
            )
                .andDocumentErrorResponse(documentationRoles, hasRequestPayload = true)
        }
    }

    @NestedTest
    inner class GrantPermissionToRole {

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should grant permission to role that exists`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/permissions")
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
                    "This doesn't check if the permission is already added, the role ID must be valid"
                )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should not grant permission to role that does not exists`() {
            mockMvc.perform(
                post("/roles/1234/permissions")
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
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = false)
        }
    }

    @NestedTest
    inner class RevokePermissionFromRole {

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should revoke permission from role that exists`() {
            mockMvc.perform(
                delete("/roles/${testRole.id}/permissions")
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
                    "Revokes permissions, it doesn't check if the role contains them."
                )
            assert(!roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should not revoke permission from role that does not exist`() {
            mockMvc.perform(
                delete("/roles/1234/permissions")
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
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = false)

        }
    }

    @NestedTest
    inner class AddUserToRole {
        @BeforeEach
        fun addRoleAndUser() {
            roleRepository.save(testRole)
            accountRepository.save(testAccount)
        }

        @Test
        fun `should add an existing account to an existing role`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationAccount,
                "Add an account to a role by its IDs",
                "It will return an error if the user already has the role or if it doesn't exist"
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not add an non existing account to an existing role`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an non existing account to an non existing role`() {
            mockMvc.perform(
                post("/roles/1234/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an existing account to an non existing role`() {
            mockMvc.perform(
                post("/roles/1234/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }
    }

    @NestedTest
    inner class RemoveUserFromRole {
        @BeforeEach
        fun addRoleAndUser() {
            accountRepository.save(testAccount)
            roleRepository.save(testRole)
            testRole.accounts.add(testAccount)
            roleRepository.save(testRole)
        }

        @AfterEach
        fun removeRolesAndUser() {
            testRole.accounts.remove(testAccount)
            roleRepository.save(testRole)
        }

        @Test
        fun `should remove an existing account from an existing role`() {
            mockMvc.perform(
                delete("/roles/${testRole.id}/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isOk
            ).andDocumentEmptyObjectResponse(
                documentationAccount,
                "Removes the account from the role by ID",
                "It only works if the user has the role and it exists"
            )
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not remove a non existing account from an existing role`() {
            mockMvc.perform(
                delete("/roles/${testRole.id}/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an non existing account to an non existing role`() {
            mockMvc.perform(
                delete("/roles/1234/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to 1234)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an existing account to an non existing role`() {
            mockMvc.perform(
                delete("/roles/1234/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("userId" to testAccount.id)))
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationAccount, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }
    }

    @NestedTest
    inner class AddPermissionToRoleActivity {
        @BeforeEach
        fun addAll() {
            roleRepository.save(testRole)
            projectRepository.save(testProject)
        }

        @Test
        fun `should add permission to role activity and create PerActivityRole`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/activities/${testProject.id}/permissions")
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
                "Adds an permission to an role activity",
                "It will create a PerRoleActivity if it doesn't exist"
            )
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
                post("/roles/1234/activities/${testProject.id}/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }

        @Test
        fun `shouldn't add permission to role activity if activityId is invalid`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/activities/1234/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )
            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }

        @Test
        fun `shouldn't add permission to role activity if activityId is invalid and roleId is invalid`() {
            mockMvc.perform(
                post("/roles/${testRole.id}/activities/1234/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = true)
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.associatedActivities.size == 0)
        }
    }

    @NestedTest
    inner class RemovePermissionFromPerRoleActivity {
        @BeforeEach
        fun addAll() {
            roleRepository.save(testRole)
            projectRepository.save(testProject)
            val perActivityRole = PerActivityRole(Permissions(listOf(Permission.EDIT_ACTIVITY)))
            perActivityRole.activity = testProject
            perActivityRole.role = testRole
            testProject.associatedRoles.add(perActivityRole)
            projectRepository.save(testProject)
            roleRepository.save(testRole)
        }

        @AfterEach
        fun removeAll() {
            testProject.associatedRoles.removeAt(0)
            projectRepository.save(testProject)
        }

        @Test
        fun `should remove an existing role activity permission`() {
            mockMvc.perform(
                delete("/roles/${testRole.id}/activities/${testProject.id}/permissions")
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
                    "doesn't check if the permissions are already revoked..."
            )
            assert(projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles.size == 1)
            assert(
                !projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role activity permission on a non existing role`() {
            mockMvc.perform(
                delete("/roles/1234/activities/${testProject.id}/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = false)
            assert(projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role activity permission on a non existing activity`() {
            mockMvc.perform(
                delete("/roles/${testRole.id}/activities/1234/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = false)
            assert(projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }

        @Test
        fun `should not remove an existing role activity perm on a non existing activity and non existing role`() {
            mockMvc.perform(
                delete("/roles/1234/activities/1234/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf("permissions" to Permissions(listOf(Permission.EDIT_ACTIVITY)))
                        )
                    )

            ).andExpectAll(
                status().isNotFound()
            ).andDocumentErrorResponse(documentationPermissions, hasRequestPayload = false)
            assert(projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles.size == 1)
            assert(
                projectRepository.findByIdOrNull(testProject.id!!)!!.associatedRoles[0].permissions.contains(
                    Permission.EDIT_ACTIVITY
                )
            )
        }
    }
}
