package pt.up.fe.ni.website.backend.dto.entity.role

import pt.up.fe.ni.website.backend.dto.entity.EntityDto
import pt.up.fe.ni.website.backend.model.Role

class UpdateRoleDto(
    val name: String,
    val isSection: Boolean?
) : EntityDto<Role>()
