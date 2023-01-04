package pt.up.fe.ni.website.backend.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.model.Account
import java.util.Date

class AccountDto(
    val email: String,
    val password: String,
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    @JsonIgnore
    var photoFile: MultipartFile?,
    val linkedin: String?,
    val github: String?,
    val websites: List<CustomWebsiteDto>?
) : Dto<Account>()
