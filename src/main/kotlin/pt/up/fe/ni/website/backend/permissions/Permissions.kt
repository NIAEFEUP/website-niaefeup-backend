package pt.up.fe.ni.website.backend.permissions

import java.util.Collections
import java.util.TreeSet

class Permissions(private val permissions: MutableSet<Permission> = Collections.emptySet()) : MutableSet<Permission> by TreeSet(permissions) {

    companion object {
        fun fromLong(encoded: Long): Permissions {
            val result = Permissions()
            for (perm in Permission.values()) {
                val encodedBit = encoded ushr perm.bit and 1L
                if (encodedBit == 1L) {
                    result.add(perm)
                }
            }

            return result
        }
    }
    fun toLong() = permissions.fold(0L) { acc, perm ->
        return acc or (1L shl perm.bit)
    }
}
