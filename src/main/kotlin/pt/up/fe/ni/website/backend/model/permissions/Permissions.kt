package pt.up.fe.ni.website.backend.model.permissions

import java.util.Collections
import java.util.TreeSet

class Permissions(
    private val permissions: Set<Permission> = Collections.emptySet()
) : MutableSet<Permission> by TreeSet(permissions) {

    companion object {
        fun fromLong(encoded: Long): Permissions {
            val decodedPermissions = Permission.values()
                .filter { perm ->
                    // To decide if the permission is present in the `encoded` Long,
                    // we need to see if the bits for those permissions are set (equal to 1)
                    // `encoded ushr n and 1L` returns the value of the `n`-th bit of `encoded`
                    val encodedBit = encoded ushr perm.bit and 1L
                    return@filter encodedBit == 1L
                }.toSet()

            return Permissions(decodedPermissions)
        }
    }

    // To encode the permissions into a Long, we need to set the
    // corresponding bit to 1, if the permission is present
    // `1L shl n` returns a number whose binary representation has
    // a single 1 in the `n`-th position
    fun toLong() = permissions.fold(0L) { acc, perm ->
        return acc or (1L shl perm.bit)
    }
}
