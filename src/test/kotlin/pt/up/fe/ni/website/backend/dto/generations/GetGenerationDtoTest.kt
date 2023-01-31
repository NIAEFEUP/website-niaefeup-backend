package pt.up.fe.ni.website.backend.dto.generations

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permissions

class GetGenerationDtoTest {
    private lateinit var testGeneration: Generation

    @BeforeEach
    fun setup() {
        testGeneration = Generation("22/23")
    }

    @Test
    fun `generation with no roles`() = testBuildGetGenerationDto(mutableListOf(), emptyList())

    @Test
    fun `generation with only non-section roles`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole("role", false),
            buildTestRole(
                "role2",
                false,
                mutableListOf(buildTestAccount("account", mutableListOf()))
            )
        ),
        emptyList()
    )

    @Test
    fun `generation with a section role and no users`() = testBuildGetGenerationDto(
        mutableListOf(buildTestRole("role", true, mutableListOf())),
        emptyList()
    )

    @Test
    fun `generation with a section role and a user with no roles`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole("role", true, mutableListOf(buildTestAccount("account", mutableListOf())))
        ),
        listOf(
            GenerationSectionDto(
                "role",
                listOf(GenerationUserDto("account", emptyList()))
            )
        )
    )

    @Test
    fun `generation with a section role and a user with a role`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole(
                "section-role",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account",
                        mutableListOf(buildTestRole("user-role", false))
                    )
                )
            )
        ),
        listOf(
            GenerationSectionDto(
                "section-role",
                listOf(GenerationUserDto("account", listOf("user-role")))
            )
        )
    )

    @Test
    fun `generation with a section role and a user with multiple roles`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole(
                "section-role",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account",
                        mutableListOf(
                            buildTestRole("user-role", false),
                            buildTestRole("user-role2", false)
                        )
                    )
                )
            )
        ),
        listOf(
            GenerationSectionDto(
                "section-role",
                listOf(GenerationUserDto("account", listOf("user-role", "user-role2")))
            )
        )
    )

    @Test
    fun `generation with a section role and multiple users`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole(
                "section-role",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account",
                        mutableListOf(buildTestRole("user-role", false))
                    ),
                    buildTestAccount(
                        "account2",
                        mutableListOf(buildTestRole("user-role2", false))
                    )
                )
            )
        ),
        listOf(
            GenerationSectionDto(
                "section-role",
                listOf(
                    GenerationUserDto("account", listOf("user-role")),
                    GenerationUserDto("account2", listOf("user-role2"))
                )
            )
        )
    )

    @Test
    fun `generation with multiple section roles and multiple users`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole(
                "section-role",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account",
                        mutableListOf(buildTestRole("user-role", false))
                    )
                )
            ),
            buildTestRole(
                "section-role2",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account2",
                        mutableListOf(buildTestRole("user-role2", false))
                    )
                )
            )
        ),
        listOf(
            GenerationSectionDto(
                "section-role",
                listOf(GenerationUserDto("account", listOf("user-role")))
            ),
            GenerationSectionDto(
                "section-role2",
                listOf(GenerationUserDto("account2", listOf("user-role2")))
            )
        )
    )

    @Test
    fun `generation with multiple section roles, users and respective roles`() = testBuildGetGenerationDto(
        mutableListOf(
            buildTestRole(
                "section-role",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account",
                        mutableListOf(
                            buildTestRole("user-role", false),
                            buildTestRole("user-role2", false)
                        )
                    )
                )
            ),
            buildTestRole(
                "section-role2",
                true,
                mutableListOf(
                    buildTestAccount(
                        "account2",
                        mutableListOf(
                            buildTestRole("user-role3", false),
                            buildTestRole("user-role4", false)
                        )
                    )
                )
            )
        ),
        listOf(
            GenerationSectionDto(
                "section-role",
                listOf(GenerationUserDto("account", listOf("user-role", "user-role2")))
            ),
            GenerationSectionDto(
                "section-role2",
                listOf(GenerationUserDto("account2", listOf("user-role3", "user-role4")))
            )
        )
    )

    private fun testBuildGetGenerationDto(generationRoles: MutableList<Role>, expected: GetGenerationDto) {
        testGeneration.roles.apply { clear(); addAll(generationRoles) }
        val actual = buildGetGenerationDto(testGeneration)
        assert(actual == expected)
    }

    private fun buildTestRole(
        name: String,
        isSection: Boolean,
        accounts: MutableList<Account> = mutableListOf()
    ): Role {
        val role = Role(name, Permissions(emptySet()), isSection, accounts)
        role.generation = testGeneration
        return role
    }

    private fun buildTestAccount(name: String, roles: MutableList<Role>) = Account(
        name, "email", "password", null, null,
        null, null, null, emptyList(), roles
    )
}
