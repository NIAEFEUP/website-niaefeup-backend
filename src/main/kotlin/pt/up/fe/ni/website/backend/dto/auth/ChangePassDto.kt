package pt.up.fe.ni.website.backend.dto.auth

data class ChangePassDto(
    val oldPassword: String,
    val newPassword: String
)
