package pt.up.fe.ni.website.backend.service.activity

import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.service.AccountService

@Service
class ActivityService(
    repository: ActivityRepository<Activity>,
    accountService: AccountService,
) : AbstractActivityService<Activity>(repository, accountService)
