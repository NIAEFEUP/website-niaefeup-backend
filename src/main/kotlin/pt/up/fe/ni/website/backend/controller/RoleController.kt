package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.auth.UserIdDto
import pt.up.fe.ni.website.backend.dto.entity.role.CreateRoleDto
import pt.up.fe.ni.website.backend.dto.entity.role.UpdateRoleDto
import pt.up.fe.ni.website.backend.dto.roles.PermissionsDto
import pt.up.fe.ni.website.backend.service.RoleService

@RestController
@RequestMapping("/roles")
class RoleController(private val roleService: RoleService) {
    @GetMapping
    fun getAllRoles() = roleService.getAllRoles()

    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRoleById(id)

    @PostMapping
    fun createNewRole(@RequestBody dto: CreateRoleDto) = roleService.createNewRole(dto)

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long) = roleService.deleteRole(id)

    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @RequestBody dto: UpdateRoleDto) = roleService.updateRole(id, dto)

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
            permissionsDto.permissions
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
            permissionsDto.permissions
        )
        return emptyMap()
    }
}
