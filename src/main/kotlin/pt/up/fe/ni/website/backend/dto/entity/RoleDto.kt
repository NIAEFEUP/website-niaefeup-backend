package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Role

class RoleDto(
    val name: String,
    val permissions: List<Int>,
    val isSection: Boolean,
    val accounts: List<Long>,
    val perActivities: List<PerActivityRoleDto>
) : EntityDto<Role>()
