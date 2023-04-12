package pt.up.fe.ni.website.backend.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.RoleDto
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.model.PerActivityRole
import pt.up.fe.ni.website.backend.model.Role
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.repository.GenerationRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository

@Service
@Transactional
class RoleService(
    private val roleRepository: RoleRepository,
    private val perActivityRoleRepository: PerActivityRoleRepository,
    private val generationRepository: GenerationRepository,
    private val accountRepository: AccountRepository,
    private val activityRepository: ActivityRepository<Activity>
) {

    fun getRole(roleId: Long): Role {
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        return role
    }
    fun getAllRoles(): List<Role> = roleRepository.findAll().toList()
    fun grantPermissionToRole(roleId: Long, permissions: Permissions) {
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        role.permissions.addAll(permissions)
        roleRepository.save(role)
    }

    fun revokePermissionFromRole(roleId: Long, permissions: Permissions) {
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        role.permissions.removeAll(permissions)
        roleRepository.save(role)
    }

    fun grantPermissionToRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityRepository.findById(activityId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.activityNotFound(activityId))
        }
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        val foundActivity = activity.associatedRoles
            .find { it.activity == activity } ?: PerActivityRole(Permissions())
        foundActivity.role = role
        foundActivity.activity = activity

        foundActivity.permissions.addAll(permissions)
        perActivityRoleRepository.save(foundActivity)
        if (activity.associatedRoles.find { it.activity == activity } == null) {
            role.associatedActivities.add(foundActivity)
        }
        activityRepository.save(activity)
        roleRepository.save(role)
    }

    fun revokePermissionFromRoleOnActivity(roleId: Long, activityId: Long, permissions: Permissions) {
        val activity = activityRepository.findById(activityId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.activityNotFound(activityId))
        }
        val role = roleRepository.findById(roleId).orElseThrow {
            throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        }
        val foundActivity = activity.associatedRoles
            .find { it.role == role } ?: return
        foundActivity.permissions.removeAll(permissions)
        perActivityRoleRepository.save(foundActivity)
        activityRepository.save(activity)

    }

    fun createNewRole(dto: RoleDto): Role {
        if (roleRepository.findByName(dto.name) != null) {
            throw IllegalArgumentException(ErrorMessages.roleAlreadyExists)
        }
        val role = dto.create()
        val latestGeneration = generationRepository.findFirstByOrderBySchoolYearDesc()
            ?: throw IllegalArgumentException(ErrorMessages.noGenerations)
        roleRepository.save(role)
        latestGeneration.roles.add(role)
        generationRepository.save(latestGeneration)
        return role
    }

    fun deleteRole(roleId: Long) {
        val role = roleRepository.findByIdOrNull(roleId)
            ?: throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        val latestGeneration = generationRepository.findFirstByOrderBySchoolYearDesc()!!
        latestGeneration.roles.remove(role)
        generationRepository.save(latestGeneration)
        roleRepository.deleteById(roleId)
    }

    fun addUserToRole(roleId: Long, userId: Long) {
        val role = roleRepository.findByIdOrNull(roleId)
            ?: throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        val account = accountRepository.findByIdOrNull(userId)
            ?: throw NoSuchElementException(ErrorMessages.accountNotFound(userId))
        role.accounts.find { it.id == account.id }.let {
            if (it != null) throw NoSuchElementException(ErrorMessages.userAlreadyHasRole(roleId, userId))
        }
        role.accounts.add(account)
        roleRepository.save(role)
    }

    fun removeUserFromRole(roleId: Long, userId: Long) {
        val role = roleRepository.findByIdOrNull(roleId)
            ?: throw NoSuchElementException(ErrorMessages.roleNotFound(roleId))
        val account = accountRepository.findByIdOrNull(userId)
            ?: throw NoSuchElementException(ErrorMessages.accountNotFound(userId))
        role.accounts.find { it.id == account.id }.let {
            if (it == null) throw NoSuchElementException(ErrorMessages.userNotInRole(roleId, userId))
        }
        role.accounts.remove(account)
        roleRepository.save(role)
    }
}
