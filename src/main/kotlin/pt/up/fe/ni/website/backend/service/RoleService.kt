package pt.up.fe.ni.website.backend.service

import jakarta.transaction.Transactional
import jakarta.validation.Validator
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.role.CreateRoleDto
import pt.up.fe.ni.website.backend.dto.entity.role.UpdateRoleDto
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository
import pt.up.fe.ni.website.backend.service.activity.ActivityService

@Service
@Transactional
class RoleService(
    private val roleRepository: RoleRepository,
    private val perActivityRoleRepository: PerActivityRoleRepository,
    private val generationService: GenerationService,
    private val accountService: AccountService,
    private val activityService: ActivityService,
    private val generationRepository: GenerationRepository,
    private val validator: Validator
) {

    fun getRoleById(roleId: Long): Role {
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        return role
    }
    fun getAllRoles(): List<Role> = roleRepository.findAll().toList()
    fun grantPermissionToRole(roleId: Long, permissions: Permissions) {
        val role = getRoleById(roleId)
        role.permissions.addAll(permissions)
        roleRepository.save(role)
    }

    fun revokePermissionFromRole(roleId: Long, permissions: Permissions) {
        val role = getRoleById(roleId)
        role.permissions.removeAll(permissions)
        roleRepository.save(role)
    }

    fun grantPermissionToRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityService.getActivityById(activityId)
        val role = getRoleById(roleId)
        val perActivityRole = activity.associatedRoles
            .find { it.activity == activity } ?: PerActivityRole(Permissions())
        perActivityRole.role = role
        perActivityRole.activity = activity

        perActivityRole.permissions.addAll(permissions)
        perActivityRoleRepository.save(perActivityRole)
    }

    fun revokePermissionFromRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityService.getActivityById(activityId)
        val role = getRoleById(roleId)
        val foundActivity = activity.associatedRoles
            .find { it.role == role } ?: return
        foundActivity.permissions.removeAll(permissions)
        perActivityRoleRepository.save(foundActivity)
    }

    fun createNewRole(dto: CreateRoleDto): Role {
        val role = dto.create()
        val generation = generationService.getGenerationByIdOrInferLatest(dto.generationId)

        generation.roles.add(role) // just for validation and will not be persisted
        if (validator.validateProperty(generation, "roles").isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessages.roleAlreadyExists(role.name, generation.schoolYear))
        }

        for (perActivityRoleDto in dto.associatedActivities) {
            val activity = activityService.getActivityById(perActivityRoleDto.activityId!!)

            for (perActivityRole in role.associatedActivities) {
                perActivityRole.role = role
                perActivityRole.activity = activity
            }
        }

        role.generation = generation
        roleRepository.save(role)
        return role
    }

    fun deleteRole(roleId: Long) {
        val role = getRoleById(roleId)
        role.generation.roles.remove(role)
        generationRepository.save(role.generation)
        roleRepository.delete(role)
    }

    fun addUserToRole(roleId: Long, userId: Long) {
        val role = getRoleById(roleId)
        val account = accountService.getAccountById(userId)
        if (role.accounts.any { it.id == account.id }) return
        account.roles.add(role)
        roleRepository.save(role)
    }

    fun removeUserFromRole(roleId: Long, userId: Long) {
        val role = getRoleById(roleId)
        val account = accountService.getAccountById(userId)
        role.accounts.remove(account)
        roleRepository.save(role)
    }

    fun updateRole(roleId: Long, dto: UpdateRoleDto): Role {
        val role = getRoleById(roleId)

        dto.update(role)
        if (validator.validateProperty(role.generation, "roles").isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessages.roleAlreadyExists(role.name, role.generation.schoolYear))
        }

        return roleRepository.save(role)
    }
}
