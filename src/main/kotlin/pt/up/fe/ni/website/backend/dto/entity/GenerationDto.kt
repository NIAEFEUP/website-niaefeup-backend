package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Generation

class GenerationDto(
    var schoolYear: String?,
    val roles: List<RoleDto>,
) : EntityDto<Generation>()
