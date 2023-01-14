package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    @JsonIgnore
    val teamMembers: MutableList<AccountDto>?,
    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList()
) : EntityDto<Project>()
