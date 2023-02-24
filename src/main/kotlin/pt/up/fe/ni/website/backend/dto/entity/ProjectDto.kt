package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    val teamMembersIds: List<Long>?,
    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    val slug: String?
) : EntityDto<Project>()
