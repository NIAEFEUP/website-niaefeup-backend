package pt.up.fe.ni.website.backend.dto.entity.account

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Date
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.entity.CustomWebsiteDto
import pt.up.fe.ni.website.backend.dto.entity.EntityDto
import pt.up.fe.ni.website.backend.model.Account

class UpdateAccountDto(
    val email: String,
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    @JsonIgnore
    var photoFile: MultipartFile?, val linkedin: String?,
    val github: String?,
    val websites: List<CustomWebsiteDto>?
) : EntityDto<Account>()
