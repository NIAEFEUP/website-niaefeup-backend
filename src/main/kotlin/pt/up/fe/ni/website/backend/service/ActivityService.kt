package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.repository.ActivityRepository

@Service
class ActivityService(private val repository: ActivityRepository, private val accountService: AccountService) {
    fun getActivityById(id: Long): Activity = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.activityNotFound(id))

    fun addTeamMemberById(idActivity: Long, idAccount: Long): Activity {
        val activity = getActivityById(idActivity)
        val account = accountService.getAccountById(idAccount)
        activity.teamMembers.add(account)
        return repository.save(activity)
    }

    fun removeTeamMemberById(idActivity: Long, idAccount: Long): Activity {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) throw NoSuchElementException(ErrorMessages.accountNotFound(idAccount))
        activity.teamMembers.removeIf { it.id == idAccount }
        return repository.save(activity)
    }
}
