package pt.up.fe.ni.website.backend.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.util.enable
import java.util.BitSet

internal class PermissionTest {
    @Test
    fun joinCustom() {
        val perm1 = Permission(1)
        val perm2 = Permission(2)

        val perm = perm1.add(perm2)
        val list = perm.permissions()

        assertEquals(2, list.size)
        assertEquals(BasePermissions.USERS.value, list[0]) // Bit 1
        assertEquals(BasePermissions.PROJECTS.value, list[1]) // Bit 2

        assertEquals("custom", perm.name)
    }

    @Test
    fun joinSame() {
        val perm1 = Permission(1)
        val perm2 = Permission(1)

        val perm = perm1.add(perm2)
        assertEquals("users", perm1)
    }

    @Test
    fun joinAll() {
        val perm1 = Permission(1)
        val perm2 = Permission(2)
        val perm3 = Permission(4)

        val allPerms = perm1.add(perm2).add(perm3)
        assertEquals("all", allPerms.name)
    }

    @Test
    fun remove() {
        val basePerm = Permission(3)
        val removePerm = Permission(2)

        val perm = basePerm.remove(removePerm)

        val list = perm.permissions()

        assertEquals(1, list.size)
        assertEquals(BasePermissions.USERS.value, list[0]) // Bit 1

        assertEquals("users", perm.name)
    }

    @Test
    fun removeCustom() {
        val basePerm = BasePermissions.ALL.value
        val removePerm = Permission(1)

        val perm = basePerm.remove(removePerm)

        val list = perm.permissions()

        assertEquals(2, list.size)
        assertEquals(BasePermissions.PROJECTS.value, list[0]) // Bit 1
        assertEquals(BasePermissions.EVENTS.value, list[1]) // Bit 2

        assertEquals("custom", perm.name)
    }

    @Test
    fun permissions() {
        val bitSet = BitSet(3)
        bitSet.enable(1)
        bitSet.enable(2)

        val permission = Permission("custom", bitSet)
        val list = permission.permissions()

        assertEquals(2, list.size)
        assertEquals(BasePermissions.PROJECTS.value, list[0]) // Bit 1
        assertEquals(BasePermissions.EVENTS.value, list[1]) // Bit 2
    }

    @Test
    fun permissionsFromLong() {
        val bitSet = BitSet(3)
        bitSet.enable(1)
        bitSet.enable(2)

        val permission = Permission(6) // 011
        val list = permission.permissions()

        assertEquals(2, list.size)
        assertEquals(BasePermissions.PROJECTS.value, list[0]) // Bit 1
        assertEquals(BasePermissions.EVENTS.value, list[1]) // Bit 2
    }

    @Test
    fun permissionsName() {
        assertEquals("none", BasePermissions.NONE.value.name)
        assertEquals("all", BasePermissions.ALL.value.name)
        assertEquals("projects", BasePermissions.PROJECTS.value.name)
        assertEquals("events", BasePermissions.EVENTS.value.name)
        assertEquals("users", BasePermissions.USERS.value.name)

        assertEquals("none", Permission(0).name)
        assertEquals("all", Permission(7).name)
        assertEquals("users", Permission(1).name)
        assertEquals("projects", Permission(2).name)
        assertEquals("events", Permission(4).name)
    }
}
