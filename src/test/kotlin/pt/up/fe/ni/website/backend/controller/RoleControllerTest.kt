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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest

@ControllerTest
@Transactional
internal class RoleControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val roleRepository: RoleRepository,
    val accountRepository: AccountRepository,
    val generationRepository: GenerationRepository
) {
    val testGeneration = Generation(
        "22-23"
    )

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
            mockMvc.get("/roles").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(roles)) }
            }
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
            mockMvc.get("/roles/${testRole.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(testRole)) }
            }
        }

        @Test
        fun `should return error on invalid roleID`() {
            mockMvc.get("/roles/4020").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors.[0].message") { value("role not found with id 4020") }
            }
        }
    }

    @NestedTest
    inner class CreateNewRole {

        @BeforeEach
        fun addRole() {
            generationRepository.save(testGeneration)
            roleRepository.save(testRole)
        }

        @Test
        fun `should add new role`() {
            mockMvc.post("/roles/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "name" to otherTestRole.name,
                        "permissions" to otherTestRole.permissions.map { it.bit },
                        "isSection" to otherTestRole.isSection,
                        "associatedActivities" to otherTestRole.associatedActivities,
                        "accounts" to otherTestRole.accounts
                    )
                )
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.name") { value(otherTestRole.name) }
                jsonPath("$.id") { value(2) } // this must be hardcoded
                jsonPath("$.permissions.length()") { value(otherTestRole.permissions.size) }
                jsonPath("$.isSection") { value(otherTestRole.isSection) }
                jsonPath("$.associatedActivities") { value(otherTestRole.associatedActivities) }
            }
            assert(roleRepository.existsById(2))
            assert(generationRepository.findFirstByOrderBySchoolYearDesc() != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 1)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles[0].id!!.compareTo(2) == 0)
        }

        @Test
        fun `shouldn't add role with same name`() {
            mockMvc.post("/roles/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "name" to testRole.name,
                        "permissions" to testRole.permissions.map { it.bit },
                        "isSection" to testRole.isSection,
                        "associatedActivities" to testRole.associatedActivities,
                        "accounts" to testRole.accounts
                    )
                )
            }.andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors.[0].message") { value("role already exists") }
            }
            assert(roleRepository.findByName(testRole.name) != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc() != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 1)
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

        @Test
        fun `should remove role with correct id`() {
            mockMvc.delete("/roles/${testRole.id}").andExpect {
                status { isOk() }
            }
            assert(roleRepository.findByName(testRole.name) == null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc() != null)
            assert(generationRepository.findFirstByOrderBySchoolYearDesc()!!.roles.size == 0)
        }

        @Test
        fun `should not remove role id that does not exist`() {
            mockMvc.delete("/roles/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors.[0].message") { value("role not found with id 1234") }
            }
        }
    }

    @NestedTest
    inner class GrantPermissionRole {

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should grant permission to role that exists`() {
            mockMvc.post("/roles/${testRole.id}/grant") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    Permissions(listOf(Permission.SUPERUSER))
                )
            }.andExpect {
                status { isOk() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should grant permission to role that does not exists`() {
            mockMvc.post("/roles/1234/grant") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    Permissions(listOf(Permission.SUPERUSER))
                )
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @NestedTest
    inner class RevokePermissionRole {

        @BeforeEach
        fun addRole() {
            roleRepository.save(testRole)
        }

        @Test
        fun `should revoke permission to role that exists`() {
            mockMvc.post("/roles/${testRole.id}/revoke") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    Permissions(listOf(Permission.SUPERUSER))
                )
            }.andExpect {
                status { isOk() }
            }
            assert(!roleRepository.findByIdOrNull(testRole.id!!)!!.permissions.contains(Permission.SUPERUSER))
        }

        @Test
        fun `should revoke permission to role that does not exists`() {
            mockMvc.post("/roles/1234/revoke") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    Permissions(listOf(Permission.SUPERUSER))
                )
            }.andExpect {
                status { isNotFound() }
            }
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
            mockMvc.post("/roles/${testRole.id}/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testAccount.id)
            }.andExpect {
                status { isOk() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not add an non existing account to an existing role`() {
            mockMvc.post("/roles/${testRole.id}/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(1234)
            }.andExpect {
                status { isNotFound() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an non existing account to an non existing role`() {
            mockMvc.post("/roles/1234/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(1234)
            }.andExpect {
                status { isNotFound() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not add an existing account to an non existing role`() {
            mockMvc.post("/roles/1234/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testAccount.id)
            }.andExpect {
                status { isNotFound() }
            }
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
        fun `should remove an existing account to an existing role`() {
            mockMvc.delete("/roles/${testRole.id}/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testAccount.id)
            }.andExpect {
                status { isOk() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size == 0)
        }

        @Test
        fun `should not remove an non existing account to an existing role`() {
            mockMvc.delete("/roles/${testRole.id}/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(1234)
            }.andExpect {
                status { isNotFound() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an non existing account to an non existing role`() {
            mockMvc.delete("/roles/1234/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(1234)
            }.andExpect {
                status { isNotFound() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }

        @Test
        fun `should not remove an existing account to an non existing role`() {
            mockMvc.delete("/roles/1234/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testAccount.id)
            }.andExpect {
                status { isNotFound() }
            }
            assert(roleRepository.findByIdOrNull(testRole.id!!)!!.accounts.size != 0)
        }
    }
}
