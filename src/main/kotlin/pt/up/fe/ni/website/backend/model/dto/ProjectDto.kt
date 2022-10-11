package pt.up.fe.ni.website.backend.model.dto

import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val name: String,
    val description: String
) : Dto<Project>()
