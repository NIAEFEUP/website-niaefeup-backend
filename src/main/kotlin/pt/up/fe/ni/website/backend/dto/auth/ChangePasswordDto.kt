package pt.up.fe.ni.website.backend.dto.auth

data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String
)
