package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.repository.ActivityRepository

@Service
abstract class ActivityService<T : Activity>(
    private val repository: ActivityRepository<T>,
    private val accountService: AccountService
) {
    private fun getActivityById(id: Long): T = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.activityNotFound(id))

    fun addTeamMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        val account = accountService.getAccountById(idAccount)
        activity.teamMembers.add(account)
        return repository.save(activity)
    }

    fun removeTeamMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        activity.teamMembers.removeIf { it.id == idAccount }
        return repository.save(activity)
    }

    fun addHallOfFameMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        // TODO: Add Account to HallOfFame
        return repository.save(activity)
    }

    fun removeHallOfFameMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        // TODO: Remove Account from HallOfFame
        return repository.save(activity)
    }
    fun moveMemberToHallOfFameById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        // TODO: Move Account from Team to HallOfFame
        return repository.save(activity);
    }

    fun moveMemberToActiveTeamById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        // TODO: Move Account from HallOfFame to Team
        return repository.save(activity);
    }
}
