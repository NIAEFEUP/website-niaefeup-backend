package pt.up.fe.ni.website.backend.permissions

import java.util.TreeSet

class Permissions(val perms: Set<Permission>) : Set<Permission> by perms {

    private val permissions = TreeSet<Permission>()

    init {
        this.permissions = TreeSet<Permission>().also {
            it.addAll(permissions)
        }
    }

    fun hasPermission(permission: Permission) {
        return 
    }
}
