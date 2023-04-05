package pt.up.fe.ni.website.backend.dto.entity.account

import java.util.Date
import pt.up.fe.ni.website.backend.dto.entity.CustomWebsiteDto
import pt.up.fe.ni.website.backend.dto.entity.EntityDto
import pt.up.fe.ni.website.backend.model.Account

class CreateAccountDto(
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
