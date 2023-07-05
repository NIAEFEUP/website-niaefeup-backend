package pt.up.fe.ni.website.backend.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
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
        val foundPerActivityRole = activity.associatedRoles
            .find { it.activity == activity } ?: PerActivityRole(Permissions())
        foundPerActivityRole.role = role
        foundPerActivityRole.activity = activity

        foundPerActivityRole.permissions.addAll(permissions)
        perActivityRoleRepository.save(foundPerActivityRole)
        if (activity.associatedRoles.find { it.activity == activity } == null) {
            role.associatedActivities.add(foundPerActivityRole)
        }
        // activityRepository.save(activity)
        roleRepository.save(role)
    }

    fun revokePermissionFromRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityService.getActivityById(activityId)
        val role = getRole(roleId)
        val foundActivity = activity.associatedRoles
            .find { it.role == role } ?: return
        foundActivity.permissions.removeAll(permissions)
        perActivityRoleRepository.save(foundActivity)
        // activityRepository.save(activity)
    }

    fun createNewRole(dto: RoleDto): Role {
        if (roleRepository.findByName(dto.name) != null) {
            throw IllegalArgumentException(ErrorMessages.roleAlreadyExists)
        }
        val role = dto.create()

        val latestGeneration = generationRepository.findFirstByOrderBySchoolYearDesc()
            ?: throw IllegalArgumentException(ErrorMessages.noGenerations)

        // need to set it from both sides due to testing transaction
        role.generation = latestGeneration
        roleRepository.save(role)
        latestGeneration.roles.add(role)
        generationRepository.save(latestGeneration)
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
        role.accounts.find { it.id == account.id }.let {
            if (it != null) throw NoSuchElementException(ErrorMessages.userAlreadyHasRole(roleId, userId))
        }
        role.accounts.add(account)
        roleRepository.save(role)
    }

    fun removeUserFromRole(roleId: Long, userId: Long) {
        val role = getRole(roleId)
        val account = accountService.getAccountById(userId)
        role.accounts.find { it.id == account.id }.let {
            if (it == null) throw NoSuchElementException(ErrorMessages.userNotInRole(roleId, userId))
        }
        role.accounts.remove(account)
        roleRepository.save(role)
    }
}
