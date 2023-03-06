package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.dto.entity.GenerationDto
import pt.up.fe.ni.website.backend.dto.entity.PerActivityRoleDto
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest

@ControllerTest
@Transactional
class GenerationControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: GenerationRepository,
    val accountRepository: AccountRepository,
    val activityRepository: ActivityRepository<Activity>,
    val roleRepository: RoleRepository
) {
    private lateinit var testGeneration: Generation
    private lateinit var testGenerations: List<Generation>

    @NestedTest
    @DisplayName("GET /generations")
    inner class GetAllGenerations {

        @BeforeEach
        fun addGenerations() {
            initializeTestGenerations()
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
            initializeTestGenerations()
        }

        @Test
        fun `should return the generation of the year`() {
            mockMvc.get("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
                }
        }

        @Test
        fun `roles should be ordered`() {
            mockMvc.get("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
                }
        }

        @Test
        fun `shouldn't return repeated accounts`() {
            mockMvc.get("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].name") { value("Test Account 2") }
                }
        }

        @Test
        fun `should return non-section roles`() {
            mockMvc.get("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles[0]") { value("regular-role2") }
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
            initializeTestGenerations()
        }

        @Test
        fun `should return the generation of the id`() {
            mockMvc.get("/generations/${testGenerations[0].id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
                }
        }

        @Test
        fun `roles should be ordered`() {
            mockMvc.get("/generations/${testGenerations[0].id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
                }
        }

        @Test
        fun `shouldn't return repeated accounts`() {
            mockMvc.get("/generations/${testGenerations[0].id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].name") { value("Test Account 2") }
                }
        }

        @Test
        fun `should return non-section roles`() {
            mockMvc.get("/generations/${testGenerations[0].id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles[0]") { value("regular-role2") }
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
            initializeTestGenerations()
        }

        @Test
        fun `should return the latest generation`() {
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].section") { value("section-role1") }
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
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
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
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
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].name") { value("Test Account") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].name") { value("Test Account 2") }
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
                    jsonPath("$[0].accounts.length()") { value(1) }
                    jsonPath("$[0].accounts[0].roles.length()") { value(2) }
                    jsonPath("$[0].accounts[0].roles[0]") { value("regular-role1") }
                    jsonPath("$[0].accounts[0].roles[1]") { value("regular-role2") }
                    jsonPath("$[1].section") { value("section-role2") }
                    jsonPath("$[1].accounts.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles.length()") { value(1) }
                    jsonPath("$[1].accounts[0].roles[0]") { value("regular-role2") }
                }
        }

        @Test
        fun `should fail if no generations`() {
            repository.deleteAll()
            mockMvc.get("/generations/latest")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("no generations created yet") }
                }
        }
    }

    @NestedTest
    @DisplayName("POST /generations/new")
    inner class CreateGeneration {
        @Test
        fun `should create a new generation`() {
            mockMvc.post("/generations/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(GenerationDto("22-23", emptyList()))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.schoolYear") { value("22-23") }
                }
        }

        @Test
        fun `should create a generation with roles`() {
            val generationDtoWithRoles = GenerationDto(
                "20-21",
                listOf(
                    RoleDto(
                        "role1",
                        emptyList(),
                        true,
                        emptyList(),
                        emptyList()
                    ),
                    RoleDto(
                        "role2",
                        emptyList(),
                        false,
                        emptyList(),
                        emptyList()
                    )
                )
            )

            mockMvc.post("/generations/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(generationDtoWithRoles)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.schoolYear") { value("20-21") }
                    jsonPath("$.roles.length()") { value(2) }
                    jsonPath("$.roles[0].name") { value("role1") }
                    jsonPath("$.roles[0].isSection") { value(true) }
                    jsonPath("$.roles[0].permissions.length()") { value(0) }
                    jsonPath("$.roles[0].associatedActivities.length()") { value(0) }
                    jsonPath("$.roles[1].name") { value("role2") }
                    jsonPath("$.roles[1].isSection") { value(false) }
                    jsonPath("$.roles[1].permissions.length()") { value(0) }
                    jsonPath("$.roles[1].associatedActivities.length()") { value(0) }
                }

            val roles = roleRepository.findAll().toList()
            assertEquals(2, roles.size)
            val generation = repository.findBySchoolYear("20-21")
            assertNotNull(generation)
            assert(generation!!.roles.containsAll(roles))
            assert(roles.all { it.generation == generation })
        }

        @Test
        fun `should create a generation with roles and permissions`() {
            val generationDtoWithRoles = GenerationDto(
                "20-21",
                listOf(
                    RoleDto(
                        "role1",
                        listOf(0, 1),
                        true,
                        emptyList(),
                        emptyList()
                    )
                )
            )

            mockMvc.post("/generations/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(generationDtoWithRoles)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.schoolYear") { value("20-21") }
                    jsonPath("$.roles.length()") { value(1) }
                    jsonPath("$.roles[0].name") { value("role1") }
                    jsonPath("$.roles[0].permissions.length()") { value(2) }
                    jsonPath("$.roles[0].permissions[0]") { value(Permission.values()[0].name) }
                    jsonPath("$.roles[0].permissions[1]") { value(Permission.values()[1].name) }
                }

            val roles = roleRepository.findAll().toList()
            assertEquals(1, roles.size)
            assert(roles[0].permissions.contains(Permission.values()[0]))
            assert(roles[0].permissions.contains(Permission.values()[1]))
        }

        @Test
        fun `should fail if year is not specified and there are no generations`() {
            mockMvc.post("/generations/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(GenerationDto(null, emptyList()))
            }
                .andExpect {
                    status { isUnprocessableEntity() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("no generations created yet, please specify school year") }
                }
        }

        @NestedTest
        @DisplayName("with existing generations")
        inner class WithExistingGenerations {
            @BeforeEach
            fun addGenerations() {
                initializeTestGenerations()
            }

            @Test
            fun `should infer the year if not specified and create generation`() {
                mockMvc.post("/generations/new") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(GenerationDto(null, emptyList()))
                }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$.schoolYear") { value("23-24") }
                    }
            }

            @Test
            fun `should create a generation with role and associated accounts`() {
                val generationDtoWithAccounts = GenerationDto(
                    "20-21",
                    listOf(
                        RoleDto(
                            "role1",
                            emptyList(),
                            true,
                            listOf(1, 2),
                            emptyList()
                        )
                    )
                )

                mockMvc.post("/generations/new") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(generationDtoWithAccounts)
                }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$.schoolYear") { value("20-21") }
                        jsonPath("$.roles.length()") { value(1) }
                        jsonPath("$.roles[0].name") { value("role1") }
                    }

                val role = roleRepository.findAll().toList()
                    .filter { it.generation.schoolYear == "20-21" }
                    .find { it.name == "role1" }
                assert(role != null)

                val account1 = accountRepository.findById(1).get()
                val account2 = accountRepository.findById(2).get()
                assert(account1.roles.contains(role))
                assert(account2.roles.contains(role))

                assert(role!!.accounts.contains(account1))
                assert(role.accounts.contains(account2))
            }

            @Test
            fun `should create a generation with role and associated activities`() {
                val generationDtoWithActivities = GenerationDto(
                    "20-21",
                    listOf(
                        RoleDto(
                            "role1",
                            emptyList(),
                            true,
                            emptyList(),
                            listOf(
                                PerActivityRoleDto(
                                    1,
                                    listOf(0, 1)
                                ),
                                PerActivityRoleDto(
                                    2,
                                    listOf(2)
                                )
                            )
                        )
                    )
                )

                mockMvc.post("/generations/new") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(generationDtoWithActivities)
                }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$.schoolYear") { value("20-21") }
                        jsonPath("$.roles.length()") { value(1) }
                        jsonPath("$.roles[0].associatedActivities.length()") { value(2) }
                        jsonPath("$.roles[0].associatedActivities[0].permissions.length()") { value(2) }
                        jsonPath("$.roles[0].associatedActivities[0].permissions[0]") {
                            value(
                                Permission.values()[0].name
                            )
                        }
                        jsonPath("$.roles[0].associatedActivities[0].permissions[1]") {
                            value(
                                Permission.values()[1].name
                            )
                        }
                        jsonPath("$.roles[0].associatedActivities[1].permissions.length()") { value(1) }
                        jsonPath("$.roles[0].associatedActivities[1].permissions[0]") {
                            value(
                                Permission.values()[2].name
                            )
                        }
                    }

                val role = roleRepository.findAll().toList()
                    .filter { it.generation.schoolYear == "20-21" }
                    .find { it.name == "role1" }
                assert(role != null)

                assert(role!!.associatedActivities.all { it.role == role })

                val activity1 = activityRepository.findById(1).get()
                val activity2 = activityRepository.findById(2).get()
                assert(
                    activity1.associatedRoles.any {
                        it.role == role && it.activity == activity1 &&
                            it.permissions.contains(Permission.values()[0]) &&
                            it.permissions.contains(Permission.values()[1])
                    }
                )
                assert(
                    activity2.associatedRoles.any {
                        it.role == role && it.activity == activity2 &&
                            it.permissions.contains(Permission.values()[2])
                    }
                )
            }

            @Test
            fun `should fail if the year already exists`() {
                mockMvc.post("/generations/new") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(GenerationDto("22-23", emptyList()))
                }
                    .andExpect {
                        status { isUnprocessableEntity() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$.errors.length()") { value(1) }
                        jsonPath("$.errors[0].message") { value("generation already exists") }
                    }
            }
        }
    }

    @NestedTest
    @DisplayName("PATCH /generations/year")
    inner class UpdateGenerationByYear {
        @BeforeEach
        fun addGenerations() {
            initializeTestGenerations()
        }

        @Test
        fun `should update the generation year`() {
            mockMvc.patch("/generations/${testGenerations[0].schoolYear}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20")
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
                    mapOf("schoolYear" to "19-20")
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
            mockMvc.patch("/generations/${testGenerations[0].schoolYear}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "21-22")
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
            mockMvc.patch("/generations/${testGenerations[0].schoolYear}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "123")
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
            initializeTestGenerations()
        }

        @Test
        fun `should update the generation year`() {
            mockMvc.patch("/generations/${testGenerations[0].id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "19-20")
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
                    mapOf("schoolYear" to "19-20")
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
            mockMvc.patch("/generations/${testGenerations[0].id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("schoolYear" to "21-22")
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
                    mapOf("schoolYear" to "123")
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
    @DisplayName("DELETE /generations/year")
    inner class DeleteGenerationByYear {
        @BeforeEach
        fun addGenerations() {
            initializeTestGenerations()
        }

        @Test
        fun `should delete the generation`() {
            mockMvc.delete("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$") { isEmpty() }
                }

            assert(repository.findById(testGenerations[0].id!!).isEmpty)
        }

        @Test
        fun `should fail if the generation does not exist`() {
            mockMvc.delete("/generations/17-18")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with year 17-18") }
                }
        }

        @Test
        fun `should cascade delete the generation roles`() {
            mockMvc.delete("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$") { isEmpty() }
                }

            val roles = roleRepository.findAll().filter { it.generation.id == testGenerations[0].id }
            assert(roles.isEmpty())
        }

        @Test
        fun `should not cascade delete the role accounts`() {
            val accountNumber = accountRepository.count()

            mockMvc.delete("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$") { isEmpty() }
                }

            assertEquals(accountNumber, accountRepository.count())
        }

        @Test
        fun `should not cascade delete the role associated activities`() {
            val activityNumber = activityRepository.count()

            mockMvc.delete("/generations/${testGenerations[0].schoolYear}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$") { isEmpty() }
                }

            assertEquals(activityNumber, activityRepository.count())
        }
    }

    @NestedTest
    @DisplayName("DELETE /generations/id")
    inner class DeleteGenerationById {
        @BeforeEach
        fun addGenerations() {
            initializeTestGenerations()
        }

        @Test
        fun `should delete the generation`() {
            mockMvc.delete("/generations/${testGenerations[0].id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$") { isEmpty() }
                }

            assert(repository.findById(testGenerations[0].id!!).isEmpty)
        }

        @Test
        fun `should fail if the generation does not exist`() {
            mockMvc.delete("/generations/123")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("generation not found with id 123") }
                }
        }
    }

    private fun initializeTestGenerations() {
        val testAccount = Account(
            "Test Account",
            "test-account@gmail.com",
            "12345678",
            null,
            null,
            null,
            null,
            null,
            emptyList()
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
            emptyList()
        )

        testGeneration = buildTestGeneration(
            "22-23",
            listOf(
                buildTestRole(
                    "section-role1",
                    true,
                    listOf(testAccount),
                    listOf(
                        buildTestPerActivityRole(
                            Project("NIJobs", "cool project")
                        )
                    )
                ),
                buildTestRole(
                    "section-role2",
                    true,
                    listOf(testAccount, testAccount2),
                    emptyList()
                ),
                buildTestRole(
                    "regular-role1",
                    false,
                    listOf(testAccount),
                    listOf(
                        buildTestPerActivityRole(
                            Event(
                                title = "SINF",
                                description = "cool event",
                                dateInterval = DateInterval(TestUtils.createDate(2023, 9, 10)),
                                location = null,
                                category = null,
                                thumbnailPath = "https://www.google.com"
                            )
                        )
                    )
                ),
                buildTestRole(
                    "regular-role2",
                    false,
                    listOf(testAccount, testAccount2),
                    emptyList()
                )
            )
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
                        emptyList()
                    ),
                    buildTestRole(
                        "regular-role1",
                        false,
                        listOf(testAccount),
                        emptyList()
                    )
                )
            )
        )

        testGenerations.forEach(::saveGeneration)
    }

    private fun saveGeneration(generation: Generation) {
        generation.roles.forEach { role ->
            accountRepository.saveAll(role.accounts)
            activityRepository.saveAll(role.associatedActivities.map { it.activity })
        }
        repository.save(generation)

        // Add inverse relationships
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
        associatedActivities: List<PerActivityRole> = emptyList()
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
