package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.model.PerActivityRole

class PerActivityRoleDto(
    @JsonProperty(required = true)
    val activityId: Long?,

    val permissions: List<Int>
) : EntityDto<PerActivityRole>()
