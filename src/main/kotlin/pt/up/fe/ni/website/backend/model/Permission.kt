package pt.up.fe.ni.website.backend.model

import pt.up.fe.ni.website.backend.util.bitSet
import pt.up.fe.ni.website.backend.util.enable
import java.util.BitSet

var NUM_PERMISSIONS: Int = 3

class Permission(
    var name: String,
    private val flag: BitSet
) {

    constructor(bitSet: BitSet) : this("", bitSet) {
        name = permissionName(bitSet)
    }

    constructor(value: Long) : this(value.bitSet())

    fun add(permission: Permission): Permission {
        val newBitSet = BitSet(NUM_PERMISSIONS)
        newBitSet.or(flag)
        newBitSet.or(permission.flag)

        return Permission(newBitSet)
    }

    fun remove(permission: Permission): Permission {
        val newBitSet = BitSet(NUM_PERMISSIONS)
        newBitSet.or(permission.flag)
        newBitSet.flip(0, NUM_PERMISSIONS)
        newBitSet.and(flag)

        return Permission(newBitSet)
    }

    fun permissions(): List<Permission> {
        val permissions: ArrayList<Permission> = ArrayList()
        var bit: Int = 0

        while (bit < NUM_PERMISSIONS) {
            bit = flag.nextSetBit(bit)

            if (bit < 0) {
                break
            }

            val permission: Permission = BasePermissions.fromBit(bit)
            permissions.add(permission)

            bit ++
        }

        return permissions
    }

    private fun permissionName(bitSet: BitSet): String {
        return when (bitSet.cardinality()) {
            NUM_PERMISSIONS -> "all"
            0 -> "none"
            1 -> {
                return when (bitSet.nextSetBit(0)) {
                    0 -> "users"
                    1 -> "projects"
                    2 -> "events"
                    else -> ""
                }
            }
            else -> "custom"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permission

        if (name != other.name) return false
        if (flag != other.flag) return false

        return true
    }
}

enum class BasePermissions(val value: Permission) {
    USERS(Permission(BitSet(NUM_PERMISSIONS).enable(0))),
    PROJECTS(Permission(BitSet(NUM_PERMISSIONS).enable(1))),
    EVENTS(Permission(BitSet(NUM_PERMISSIONS).enable(2))),
    ALL(Permission(BitSet(NUM_PERMISSIONS).enable(0, NUM_PERMISSIONS))),
    NONE(Permission(BitSet(NUM_PERMISSIONS)));

    companion object {
        fun fromBit(i: Int): Permission {
            return BasePermissions.values()[i].value
        }
    }
}
