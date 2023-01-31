package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.model.Role

class RoleDto(
    val name: String,
    val permissions: List<Int>,

    @JsonProperty(required = true)
    val isSection: Boolean,

    val accountIds: List<Long> = emptyList(),
    val perActivities: List<PerActivityRoleDto>
) : EntityDto<Role>()
