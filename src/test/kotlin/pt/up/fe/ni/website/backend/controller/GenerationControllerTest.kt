package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.web.servlet.function.RequestPredicates.contentType
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest

@ControllerTest
@Transactional
class GenerationControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: GenerationRepository,
    val accountRepository: AccountRepository,
    val activityRepository: ProjectRepository, // TODO: Change to ActivityRepository
) {
    private lateinit var testGenerations: List<Generation>

    @NestedTest
    @DisplayName("GET /generations")
    inner class GetAllGenerations {

        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should return all generation years`() {
            mockMvc.get("/generations")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0]") { value("22-23") }
                    jsonPath("$[1]") { value("21-22") }
                }
        }
    }

    @NestedTest
    @DisplayName("GET /generations/year")
    inner class GetGenerationByYear {
        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should return the generation of the year`() {
            mockMvc.get("/generations/22-23")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) } // TODO: Change to accounts?
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                }
        }

        @Test
        fun `roles should be ordered`() {
            mockMvc.get("/generations/22-23")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                }
        }

        @Test
        fun `shouldn't return repeated accounts`() {
            mockMvc.get("/generations/22-23")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].name") { value("Test Account 2") }
                }
        }

        @Test
        fun `should return non-section roles`() {
            mockMvc.get("/generations/22-23")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].roles.length()") { value(1) }
                    jsonPath("$[1].users[0].roles[0]") { value("regular-role2") }
                }
        }

        @Test
        fun `should fail if the year does not exit`() {
            mockMvc.get("/generations/14-15")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with year 14-15") }
                }
        }
    }

    @NestedTest
    @DisplayName("GET /generations/id")
    inner class GetGenerationById {
        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should return the generation of the id`() {
            mockMvc.get("/generations/1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) } // TODO: Change to accounts?
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                }
        }

        @Test
        fun `roles should be ordered`() {
            mockMvc.get("/generations/1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                }
        }

        @Test
        fun `shouldn't return repeated accounts`() {
            mockMvc.get("/generations/1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].name") { value("Test Account 2") }
                }
        }

        @Test
        fun `should return non-section roles`() {
            mockMvc.get("/generations/1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].roles.length()") { value(1) }
                    jsonPath("$[1].users[0].roles[0]") { value("regular-role2") }
                }
        }

        @Test
        fun `should fail if the year does not exit`() {
            mockMvc.get("/generations/123")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with id 123") }
                }
        }
    }

    @NestedTest
    @DisplayName("GET /generations/latest")
    inner class GetLatestGeneration {
        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should return the generation of the id`() {
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) } // TODO: Change to accounts?
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                }
        }

        @Test
        fun `roles should be ordered`() {
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                }
        }

        @Test
        fun `shouldn't return repeated accounts`() {
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].name") { value("Test Account 2") }
                }
        }

        @Test
        fun `should return non-section roles`() {
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
                    jsonPath("$[0].users[0].roles.length()") { value(2) }
                    jsonPath("$[0].users[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].users[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].users.length()") { value(1) }
                    jsonPath("$[1].users[0].roles.length()") { value(1) }
                    jsonPath("$[1].users[0].roles[0]") { value("regular-role2") }
                }
        }
    }

    @NestedTest
    @DisplayName("PATCH /generations/year")
    inner class UpdateGenerationByYear {
        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should update the generation year`() {
            mockMvc.patch("/generations/22-23") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20"),
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.schoolYear") { value("19-20") }
                }
        }

        @Test
        fun `should fail if the year does not exist`() {
            mockMvc.patch("/generations/17-18") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20"),
                )
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with year 17-18") }
                }
        }

        @Test
        fun `should fail if the new year is already taken`() {
            mockMvc.patch("/generations/22-23") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "21-22"),
                )
            }
                .andExpect {
                    status { isUnprocessableEntity() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation already exists") }
                }
        }

        @Test
        fun `should fail if the new year is not valid`() {
            mockMvc.patch("/generations/22-23") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "123"),
                )
            }
                .andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("must be formatted as <xx-yy> where yy=xx+1") }
                }
        }
    }

    @NestedTest
    @DisplayName("PATCH /generations/id")
    inner class UpdateGenerationById {
        @BeforeEach
        fun addGenerations() {
            initializeGenerations()
        }

        @Test
        fun `should update the generation year`() {
            mockMvc.patch("/generations/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20"),
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.schoolYear") { value("19-20") }
                }
        }

        @Test
        fun `should fail if the generation does not exist`() {
            mockMvc.patch("/generations/123") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20"),
                )
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with id 123") }
                }
        }

        @Test
        fun `should fail if the new year is already taken`() {
            mockMvc.patch("/generations/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "21-22"),
                )
            }
                .andExpect {
                    status { isUnprocessableEntity() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation already exists") }
                }
        }

        @Test
        fun `should fail if the new year is not valid`() {
            mockMvc.patch("/generations/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "123"),
                )
            }
                .andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("must be formatted as <xx-yy> where yy=xx+1") }
                }
        }
    }

    private fun initializeGenerations() {
        val testAccount = Account(
            "Test Account",
            "test-account@gmail.com",
            "12345678",
            null,
            null,
            null,
            null,
            null,
            emptyList(),
        )

        val testAccount2 = Account(
            "Test Account 2",
            "test-account2@gmail.com",
            "12345678",
            null,
            null,
            null,
            null,
            null,
            emptyList(),
        )

        val testGeneration = buildTestGeneration(
            "22-23",
            listOf(
                buildTestRole(
                    "section-role1",
                    true,
                    listOf(testAccount),
                    listOf(
                        buildTestPerActivityRole(
                            Project("NIJobs", "cool project"),
                        ),
                    ),
                ),
                buildTestRole(
                    "section-role2",
                    true,
                    listOf(testAccount, testAccount2),
                    emptyList(),
                ),
                buildTestRole(
                    "regular-role1",
                    false,
                    listOf(testAccount),
                    listOf(
                        buildTestPerActivityRole(
                            Project("NIJobs", "cool project"),
                        ),
                    ),
                ),
                buildTestRole(
                    "regular-role2",
                    false,
                    listOf(testAccount, testAccount2),
                    emptyList(),
                ),
            ),
        )

        testGenerations = listOf(
            testGeneration,
            buildTestGeneration(
                "21-22",
                listOf(
                    buildTestRole(
                        "section-role1",
                        true,
                        listOf(testAccount),
                        emptyList(),
                    ),
                    buildTestRole(
                        "regular-role1",
                        false,
                        listOf(testAccount),
                        emptyList(),
                    ),
                ),
            ),
        )

        testGenerations.forEach(::saveGeneration)
    }

    private fun saveGeneration(generation: Generation) {
        generation.roles.forEach { role ->
            accountRepository.saveAll(role.accounts)
            activityRepository.saveAll(role.associatedActivities.map { it.activity as Project }) // TODO: Change to Activity
        }
        repository.save(generation)

        // Add inverse relationships TODO: Check
        generation.roles.forEach { role ->
            role.accounts.forEach { it.roles.add(role); accountRepository.save(it) }
            role.associatedActivities.forEach { it.role = role }
        }
        repository.save(generation)
    }

    private fun buildTestGeneration(schoolYear: String, roles: List<Role> = emptyList()): Generation {
        val generation = Generation(schoolYear)
        generation.roles.addAll(roles)
        roles.forEach { it.generation = generation }
        return generation
    }

    private fun buildTestRole(
        name: String,
        isSection: Boolean,
        accounts: List<Account> = emptyList(),
        associatedActivities: List<PerActivityRole> = emptyList(),
    ): Role {
        val role = Role(name, Permissions(emptySet()), isSection)
        role.accounts.addAll(accounts)
        role.associatedActivities.addAll(associatedActivities)
        return role
    }

    private fun buildTestPerActivityRole(activity: Activity): PerActivityRole {
        val perActivityRole = PerActivityRole(Permissions(emptySet()))
        perActivityRole.activity = activity
        return perActivityRole
    }
}
