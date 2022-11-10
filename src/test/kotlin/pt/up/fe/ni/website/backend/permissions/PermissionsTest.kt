package pt.up.fe.ni.website.backend.permissions

import org.junit.jupiter.api.Test

class PermissionsTest {

    @Test
    fun permissionsShouldBeBuilt() {
        val perms = Permissions(Permission.CREATE_ACTIVITY, Permission.CREATE_ACCOUNT)
        println(perms.permissions)
    }
}
