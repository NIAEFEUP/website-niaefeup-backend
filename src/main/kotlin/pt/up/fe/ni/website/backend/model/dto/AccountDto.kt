package pt.up.fe.ni.website.backend.model.dto

import pt.up.fe.ni.website.backend.model.Account
import java.util.Date

class AccountDto(
    val name: String,
    val email: String,
    val bio: String?,
    val birthDate: Date?,
    val photoPath: String?,
    val linkedin: String?,
    val github: String?,
    val websites: List<CustomWebsiteDto>
) : Dto<Account>()