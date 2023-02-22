package pt.up.fe.ni.website.backend.dto.entity

import java.util.Date
import pt.up.fe.ni.website.backend.model.Account

class AccountDto(
    val email: String,
    val password: String,
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    val photoPath: String?,
    val linkedin: String?,
    val github: String?,
    val websites: List<CustomWebsiteDto>?
) : EntityDto<Account>()
