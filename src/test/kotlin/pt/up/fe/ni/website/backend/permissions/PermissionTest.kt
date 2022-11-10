package pt.up.fe.ni.website.backend.permissions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PermissionTest {

    @Test
    fun permissionsShouldNotHaveTheSameBits() {
        val bitsSeen = HashSet<Long>()
        for (perm in Permission.values()) {
            Assertions.assertFalse(bitsSeen.contains(perm.bit), "Bit ${perm.bit} is used multiple times")
            Assertions.assertTrue(perm.bit < Long.SIZE_BITS, "Bit ${perm.bit} does not exceed size of Long (${Long.SIZE_BITS})")

            bitsSeen.add(perm.bit)
        }
    }
}
