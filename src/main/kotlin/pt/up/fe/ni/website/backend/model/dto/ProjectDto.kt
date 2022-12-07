package pt.up.fe.ni.website.backend.model.dto

import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    val teamMembers: MutableList<AccountDto>?, //Deve ter default value?
    val isArchived: Boolean = false
) : Dto<Project>()
