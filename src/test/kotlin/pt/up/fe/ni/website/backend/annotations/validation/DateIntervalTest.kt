package pt.up.fe.ni.website.backend.annotations.validation

import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.utils.TestUtils

internal class DateIntervalTest {
    @Test
    fun `should succeed when endDate is null`() {
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval())
        assert(
            validator.isValid(
                DateInterval(
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
        validator.initialize(ValidDateInterval())
        assert(
            validator.isValid(
                DateInterval(
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
        validator.initialize(ValidDateInterval())
        assert(
            !validator.isValid(
                DateInterval(
                    TestUtils.createDate(2022, 12, 7),
                    TestUtils.createDate(2022, 12, 6)
                ),
                null
            )
        )
    }

    @Test
    fun `should fail when endDate is equal to startDate`() {
        val date = TestUtils.createDate(2022, 12, 6)
        val validator = DateIntervalValidator()
        validator.initialize(ValidDateInterval())
        assert(
            !validator.isValid(
                DateInterval(
                    date,
                    date
                ),
                null
            )
        )
    }
}
