package pt.up.fe.ni.website.backend.model.permissions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PermissionsTest {

    @Test
    fun `permissions should be encoded correctly`() {
        val expectedBinaryString = StringBuilder("0".repeat(Long.SIZE_BITS))
            .apply {
                setCharAt(Long.SIZE_BITS - Permission.EDIT_ACTIVITY.bit - 1, '1')
                setCharAt(Long.SIZE_BITS - Permission.EDIT_SETTINGS.bit - 1, '1')
            }
            .toString()
            .toLong(2)
            .toString(2)

        Assertions.assertEquals(
            expectedBinaryString,
            Permissions(
                listOf(Permission.EDIT_ACTIVITY, Permission.EDIT_SETTINGS)
            ).toLong().toString(2)
        )
    }

    @Test
    fun `permissions should be decoded correctly`() {
        val actualLong = StringBuilder("0".repeat(Long.SIZE_BITS))
            .apply {
                setCharAt(Long.SIZE_BITS - Permission.CREATE_ACCOUNT.bit - 1, '1')
                setCharAt(Long.SIZE_BITS - Permission.DELETE_ACTIVITY.bit - 1, '1')
                setCharAt(Long.SIZE_BITS - Permission.CREATE_ACTIVITY.bit - 1, '1')
            }
            .toString()
            .toLong(2)

        Assertions.assertEquals(
            Permissions(
                listOf(Permission.CREATE_ACCOUNT, Permission.DELETE_ACTIVITY, Permission.CREATE_ACTIVITY)
            ),
            Permissions.fromLong(actualLong)
        )
    }
}
