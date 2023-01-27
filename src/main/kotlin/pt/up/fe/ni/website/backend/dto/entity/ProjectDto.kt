package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val teamMembersIds: MutableList<Long>?,
    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList()
) : EntityDto<Project>()
