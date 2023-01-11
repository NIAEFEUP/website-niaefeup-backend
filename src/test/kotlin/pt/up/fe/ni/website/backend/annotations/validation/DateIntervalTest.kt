package pt.up.fe.ni.website.backend.annotations.validation

import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.utils.TestUtils
import java.util.Date

internal class DateIntervalTest {
    @Test
    fun `should succeed when startDate is null`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval(startDate = "startDate", endDate = "endDate"))
        assert(
            validator.isValid(
                TestObject(
                    null,
                    TestUtils.createDate(2022, 12, 6)
                ),
                null
            )
        )
    }

    @Test
    fun `should succeed when endDate is null`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval(startDate = "startDate", endDate = "endDate"))
        assert(
            validator.isValid(
                TestObject(
                    TestUtils.createDate(2022, 12, 6),
                    null
                ),
                null
            )
        )
    }

    @Test
    fun `should succeed when endDate is after startDate`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval(startDate = "startDate", endDate = "endDate"))
        assert(
            validator.isValid(
                TestObject(
                    TestUtils.createDate(2022, 12, 6),
                    TestUtils.createDate(2022, 12, 7)
                ),
                null
            )
        )
    }

    @Test
    fun `should fail when endDate is before startDate`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval(startDate = "startDate", endDate = "endDate"))
        assert(
            !validator.isValid(
                TestObject(
                    TestUtils.createDate(2022, 12, 7),
                    TestUtils.createDate(2022, 12, 6)
                ),
                null
            )
        )
    }

    @Test
    fun `should fail when endDate is equal to startDate`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval(startDate = "startDate", endDate = "endDate"))
        assert(
            !validator.isValid(
                TestObject(
                    TestUtils.createDate(2022, 12, 6),
                    TestUtils.createDate(2022, 12, 6)
                ),
                null
            )
        )
    }

    internal data class TestObject(val startDate: Date?, val endDate: Date?)
}
