package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
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

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class GenerationControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: GenerationRepository,
    val accountRepository: AccountRepository,
    val activityRepository: ProjectRepository, // TODO: Change to ActivityRepository
) {
    private val testAccount = Account(
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

    private val testAccount2 = Account(
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

    private val testGeneration = buildTestGeneration(
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

    private val testGenerations = listOf(
        testGeneration,
        buildTestGeneration(
            "23-24",
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

    @Nested
    @DisplayName("GET /generations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAllGenerations {

        @BeforeAll
        fun addGenerations() {
            testGenerations.forEach(::saveGeneration)
        }

        @Test
        fun `should return all generation years`() {
            mockMvc.get("/generations")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0]") { value("23-24") }
                    jsonPath("$[1]") { value("22-23") }
                }
        }
    }

    @Nested
    @DisplayName("GET /generations/year")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    // TODO: Maybe test other cases like in GetGenerationDtoTest
    inner class GetGenerationByYear {
        @BeforeEach // TODO: It'll work after rebase
        fun addGenerations() {
            testGenerations.forEach(::saveGeneration)
        }

        @Test
        fun `should return the generation of the year`() {
            mockMvc.get("/generations/22-23")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].users.length()") { value(1) }
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
