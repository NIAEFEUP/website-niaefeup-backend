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

    @Test
    fun `should fail when first year is not 2 digits`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("2-23", null))
    }

    @Test
    fun `should fail when second year is not 2 digits`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("22-3", null))
    }

    @Test
    fun `should fail when not subsequent years`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("22-24", null))
    }

    @Test
    fun `should fail when first year after second year`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("23-22", null))
    }

    @Test
    fun `should fail when years are the same`() {
        val validator = SchoolYearValidator()
        validator.initialize(SchoolYear())
        assert(!validator.isValid("22-22", null))
    }
}
