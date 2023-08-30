package pt.up.fe.ni.website.backend.utils

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import org.springframework.test.context.transaction.TestTransaction

class TestUtils {
    companion object {
        fun createDate(year: Int, month: Int, day: Int): Date {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .apply { set(year, month, day, 0, 0, 0) }
                .time
        }

        fun startNewTransaction(rollback: Boolean = false) {
            if (rollback) {
                TestTransaction.flagForRollback()
            } else {
                TestTransaction.flagForCommit()
            }
            TestTransaction.end()
            TestTransaction.start()
        }
    }
}
