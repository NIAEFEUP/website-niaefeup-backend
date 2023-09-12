package pt.up.fe.ni.website.backend.dto.auth

import jakarta.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants

data class PasswordRecoveryConfirmDto(
    @field:Size(min = Constants.Password.minSize, max = Constants.Password.maxSize)
    val password: String
)

data class PasswordRecoveryRequestDto(
    val email: String
)
