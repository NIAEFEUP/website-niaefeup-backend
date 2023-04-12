package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.service.RoleService

@RestController
@RequestMapping("/roles")
class RoleController(private val roleService: RoleService) {
    @GetMapping
    fun getAllRoles() = roleService.getAllRoles()

    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRole(id)

    @PostMapping("/new")
    fun createNewRole(@RequestBody dto: RoleDto) = roleService.createNewRole(dto)

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): Map<String, String> {
        roleService.deleteRole(id)
        return emptyMap()
    }

    @PostMapping("/{id}/grant")
    fun grantPermissionRole(@PathVariable id: Long, @RequestBody permissions: Permissions): Map<String, String> {
        roleService.grantPermissionToRole(id, permissions)
        return emptyMap()
    }

    @PostMapping("/{id}/revoke")
    fun revokePermissionRole(@PathVariable id: Long, @RequestBody permissions: Permissions): Map<String, String> {
        roleService.revokePermissionFromRole(id, permissions)
        return emptyMap()
    }

    @PostMapping("/{id}/users")
    fun addUserToRole(@PathVariable id: Long, @RequestBody userId: Long): Map<String, String> {
        roleService.addUserToRole(id, userId)
        return emptyMap()
    }

    @DeleteMapping("/{id}/users")
    fun removeUserFromRole(@PathVariable id: Long, @RequestBody userId: Long): Map<String, String> {
        roleService.removeUserFromRole(id, userId)
        return emptyMap()
    }

    @PostMapping("/{id}/activities/{activityId}/permissions")
    fun addPermissionToPerActivityRole(
        @PathVariable id: Long,
        @PathVariable activityId: Long,
        @RequestBody permissions: Permissions
    ): Map<String, String> {
        roleService.grantPermissionToRoleOnActivity(id, activityId, permissions)
        return emptyMap()
    }

    @DeleteMapping("/{id}/activities/{activityId}/permissions")
    fun revokePermissionToPerActivityRole(
        @PathVariable id: Long,
        @PathVariable activityId: Long,
        @RequestBody permissions: Permissions
    ): Map<String, String> {
        roleService.revokePermissionFromRoleOnActivity(id, activityId, permissions)
        return emptyMap()
    }
}
