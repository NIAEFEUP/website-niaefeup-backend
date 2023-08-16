package pt.up.fe.ni.website.backend.dto.entity.role

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.dto.entity.EntityDto
import pt.up.fe.ni.website.backend.dto.entity.PerActivityRoleDto
import pt.up.fe.ni.website.backend.model.Role

class CreateRoleDto(
    val name: String,
    val permissions: List<Int>,

    @JsonProperty(required = true)
    val isSection: Boolean?,

    val accountIds: List<Long> = emptyList(),
    val associatedActivities: List<PerActivityRoleDto>,

    val generationId: Long? = null
) : EntityDto<Role>()
