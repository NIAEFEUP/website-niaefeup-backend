package pt.up.fe.ni.website.backend.model.permissions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PermissionTest {

    @Test
    fun `permissions should not have the same bits`() {
        val bitsSeen = HashSet<Int>()
        for (perm in Permission.values()) {
            Assertions.assertFalse(bitsSeen.contains(perm.bit), "Bit ${perm.bit} is used multiple times")
            bitsSeen.add(perm.bit)
        }
    }

    @Test
    fun `permission bits should not exceed Long size`() {
        for (perm in Permission.values()) {
            Assertions.assertTrue(
                perm.bit < Long.SIZE_BITS,
                "Bit ${perm.bit} (permission ${perm.name}) exceeds exceed size of Long (${Long.SIZE_BITS})"
            )
        }
    }
}
