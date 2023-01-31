package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Generation

class GenerationDto(
    val schoolYear: String,
    val roles: List<RoleDto>
) : EntityDto<Generation>()
