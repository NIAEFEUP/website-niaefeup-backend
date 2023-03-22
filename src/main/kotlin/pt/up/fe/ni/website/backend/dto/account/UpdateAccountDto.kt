package pt.up.fe.ni.website.backend.dto.account

import java.util.Date
import pt.up.fe.ni.website.backend.dto.entity.CustomWebsiteDto

class UpdateAccountDto(
    val email: String,
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    val photoPath: String?,
    val linkedin: String?,
    val github: String?,
    val websites: List<CustomWebsiteDto>?
)
