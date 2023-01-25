package pt.up.fe.ni.website.backend.service

import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val perActivityRoleRepository: PerActivityRoleRepository
) {

    fun grantPermissionToRole(role: Role, permission: Permission) {
        role.permissions.add(permission)
        roleRepository.save(role)
    }

    fun revokePermissionFromRole(role: Role, permission: Permission) {
        role.permissions.remove(permission)
        roleRepository.save(role)
    }

    fun grantPermissionToRoleOnActivity(role: Role, activity: Activity, permission: Permission) {
        val foundActivity = activity.associatedRoles
            .find { it.activity == activity } ?: PerActivityRole(role, activity, Permissions())

        foundActivity.permissions.add(permission)
        perActivityRoleRepository.save(foundActivity)
    }

    fun revokePermissionToRoleOnActivity(role: Role, activity: Activity, permission: Permission) {
        val foundActivity = activity.associatedRoles
            .find { it.activity == activity } ?: return

        foundActivity.permissions.remove(permission)
        perActivityRoleRepository.save(foundActivity)
    }
}
