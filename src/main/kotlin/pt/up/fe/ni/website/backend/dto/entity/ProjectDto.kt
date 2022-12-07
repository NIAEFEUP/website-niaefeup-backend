package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    val teamMembers: MutableList<AccountDto>, //Deve ter default value?
) : EntityDto<Project>()
