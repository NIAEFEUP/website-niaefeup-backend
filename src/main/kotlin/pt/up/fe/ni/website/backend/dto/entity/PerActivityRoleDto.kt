package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.PerActivityRole

class PerActivityRoleDto(
    val activity: Long,
    val permissions: List<Int>
) : EntityDto<PerActivityRole>()
