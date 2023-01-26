package pt.up.fe.ni.website.backend.annotations.validation

import org.junit.jupiter.api.Test

internal class SchoolYearTest {
    @Test
    fun `should succeed when right format`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(validator.isValid("22-23", null))
    }

    @Test
    fun `should fail when wrong format`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("22/23", null))
    }

    @Test
    fun `should fail when given random string`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("random_string_123", null))
    }

    @Test
    fun `should fail when given empty string`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("", null))
    }

    @Test
    fun `should fail when given blank string`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("      ", null))
    }
}
