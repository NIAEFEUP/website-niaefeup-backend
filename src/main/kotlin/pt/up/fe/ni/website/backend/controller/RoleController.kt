package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
import pt.up.fe.ni.website.backend.dto.permissions.PermissionsDto
import pt.up.fe.ni.website.backend.dto.permissions.UserIdDto
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.service.RoleService

@RestController
@RequestMapping("/roles")
class RoleController(private val roleService: RoleService) {
    @GetMapping
    fun getAllRoles() = roleService.getAllRoles()

    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRole(id)

    @PostMapping
    fun createNewRole(@RequestBody dto: RoleDto) = roleService.createNewRole(dto)

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long) = roleService.deleteRole(id)

    @PostMapping("/{id}/permissions")
    fun grantPermissionToRole(
        @PathVariable id: Long,
        @RequestBody permissionsDto: PermissionsDto
    ): Map<String, String> {
        roleService.grantPermissionToRole(id, permissionsDto.permissions)
        return emptyMap()
    }

    @DeleteMapping("/{id}/permissions")
    fun revokePermissionFromRole(
        @PathVariable id: Long,
        @RequestBody permissionsDto: PermissionsDto
    ): Map<String, String> {
        roleService.revokePermissionFromRole(id, permissionsDto.permissions)
        return emptyMap()
    }

    @PostMapping("/{id}/users")
    fun addUserToRole(@PathVariable id: Long, @RequestBody userIdDto: UserIdDto): Map<String, String> {
        roleService.addUserToRole(id, userIdDto.userId)
        return emptyMap()
    }

    @DeleteMapping("/{id}/users")
    fun removeUserFromRole(@PathVariable id: Long, @RequestBody userIdDto: UserIdDto): Map<String, String> {
        roleService.removeUserFromRole(id, userIdDto.userId)
        return emptyMap()
    }

    @PostMapping("/{id}/activities/{activityId}/permissions")
    fun addPermissionToPerActivityRole(
        @PathVariable id: Long,
        @PathVariable activityId: Long,
        @RequestBody permissionsDto: PermissionsDto
    ): Map<String, String> {
        roleService.grantPermissionToRoleOnActivity(
            id,
            activityId,
            Permissions(permissionsDto.permissions)
        )
        return emptyMap()
    }

    @DeleteMapping("/{id}/activities/{activityId}/permissions")
    fun revokePermissionFromPerActivityRole(
        @PathVariable id: Long,
        @PathVariable activityId: Long,
        @RequestBody permissionsDto: PermissionsDto
    ): Map<String, String> {
        roleService.revokePermissionFromRoleOnActivity(
            id,
            activityId,
            Permissions(permissionsDto.permissions)
        )
        return emptyMap()
    }
}
