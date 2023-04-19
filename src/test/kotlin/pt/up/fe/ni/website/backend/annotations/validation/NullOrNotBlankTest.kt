package pt.up.fe.ni.website.backend.annotations.validation

import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlank
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlankValidator

internal class NullOrNotBlankTest {
    @Test
    fun `should succeed when null`() {
        val validator = NullOrNotBlankValidator()
        validator.initialize(NullOrNotBlank())
        assert(validator.isValid(null, null))
    }

    @Test
    fun `should succeed when not blank`() {
        val validator = NullOrNotBlankValidator()
        validator.initialize(NullOrNotBlank())
        assert(validator.isValid("not blank", null))
    }

    @Test
    fun `should fail when empty`() {
        val validator = NullOrNotBlankValidator()
        validator.initialize(NullOrNotBlank())
        assert(!validator.isValid("", null))
    }

    @Test
    fun `should fail when blank`() {
        val validator = NullOrNotBlankValidator()
        validator.initialize(NullOrNotBlank())
        assert(!validator.isValid("      ", null))
    }
}
