package pt.up.fe.ni.website.backend.service

import jakarta.transaction.Transactional
import jakarta.validation.Validator
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.config.ApplicationContextUtils
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
import pt.up.fe.ni.website.backend.model.Generation
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
    private val generationRepository: GenerationRepository,
    private val accountService: AccountService,
    private val activityService: ActivityService
) {

    fun getRole(roleId: Long): Role {
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        return role
    }
    fun getAllRoles(): List<Role> = roleRepository.findAll().toList()
    fun grantPermissionToRole(roleId: Long, permissions: Permissions) {
        val role = getRole(roleId)
        role.permissions.addAll(permissions)
        roleRepository.save(role)
    }

    fun revokePermissionFromRole(roleId: Long, permissions: Permissions) {
        val role = getRole(roleId)
        role.permissions.removeAll(permissions)
        roleRepository.save(role)
    }

    fun grantPermissionToRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityService.getActivityById(activityId)
        val role = getRole(roleId)
        val perActivityRole = activity.associatedRoles
            .find { it.activity == activity } ?: PerActivityRole(Permissions())
        perActivityRole.role = role
        perActivityRole.activity = activity

        perActivityRole.permissions.addAll(permissions)
        perActivityRoleRepository.save(perActivityRole)
    }

    fun revokePermissionFromRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityService.getActivityById(activityId)
        val role = getRole(roleId)
        val foundActivity = activity.associatedRoles
            .find { it.role == role } ?: return
        foundActivity.permissions.removeAll(permissions)
        perActivityRoleRepository.save(foundActivity)
    }

    fun createNewRole(dto: RoleDto): Role {
        val role = dto.create()
        val generation: Generation = if (dto.generationId != null) {
            generationRepository.findById(dto.generationId).orElseThrow {
                IllegalArgumentException(ErrorMessages.generationNotFound(dto.generationId))
            }
        } else {
            generationRepository.findFirstByOrderBySchoolYearDesc()
                ?: throw IllegalArgumentException(ErrorMessages.noGenerations)
        }

        role.generation = generation
        generation.roles.add(role)
        val validator: Validator = ApplicationContextUtils.getBean(Validator::class.java)
        // we can infer that if something goes wrong is with adding a new role with the same name
        if (validator.validate(generation).isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessages.roleAlreadyExists(role.name, generation.schoolYear))
        }
        roleRepository.save(role)
        return role
    }

    fun deleteRole(roleId: Long) {
        val role = getRole(roleId)
        role.generation.roles.remove(role)
        generationRepository.save(role.generation)
        roleRepository.delete(role)
    }

    fun addUserToRole(roleId: Long, userId: Long) {
        val role = getRole(roleId)
        val account = accountService.getAccountById(userId)
        if (role.accounts.any { it.id == account.id }) return
        role.accounts.add(account)
        roleRepository.save(role)
    }

    fun removeUserFromRole(roleId: Long, userId: Long) {
        val role = getRole(roleId)
        val account = accountService.getAccountById(userId)
        role.accounts.remove(account)
        roleRepository.save(role)
    }
}
