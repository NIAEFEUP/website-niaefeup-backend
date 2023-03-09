package pt.up.fe.ni.website.backend.annotations.validation

import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permissions

internal class NoDuplicateRolesTest {
    @Test
    fun `should succeed with empty list`() {
        val validator = NoDuplicateRolesValidator()
        validator.initialize(NoDuplicateRoles())
        assert(validator.isValid(emptyList(), null))
    }

    @Test
    fun `should succeed with one role`() {
        val validator = NoDuplicateRolesValidator()
        validator.initialize(NoDuplicateRoles())
        val roles = listOf(buildTestRole("role"))
        assert(validator.isValid(roles, null))
    }

    @Test
    fun `should succeed with unique roles`() {
        val validator = NoDuplicateRolesValidator()
        validator.initialize(NoDuplicateRoles())
        val roles = listOf(buildTestRole("role1"), buildTestRole("role2"))
        assert(validator.isValid(roles, null))
    }

    @Test
    fun `should fail with only duplicate roles`() {
        val validator = NoDuplicateRolesValidator()
        validator.initialize(NoDuplicateRoles())
        val roles = listOf(buildTestRole("role"), buildTestRole("role"))
        assert(!validator.isValid(roles, null))
    }

    @Test
    fun `should fail with duplicate roles`() {
        val validator = NoDuplicateRolesValidator()
        validator.initialize(NoDuplicateRoles())
        val roles = listOf(buildTestRole("role1"), buildTestRole("role2"), buildTestRole("role1"))
        assert(!validator.isValid(roles, null))
    }

    private fun buildTestRole(name: String) = Role(name, Permissions(emptySet()), false, mutableListOf())
}
