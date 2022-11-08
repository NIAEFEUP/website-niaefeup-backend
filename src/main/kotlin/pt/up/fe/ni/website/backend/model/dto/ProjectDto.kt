package pt.up.fe.ni.website.backend.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants

class ProjectDto(
    @field:Schema(
        description = "Name of the project",
        example = "Uni",
        type = "string",
        minLength = Constants.Name.minSize,
        maxLength = Constants.Name.maxSize
    )
    val name: String,

    @field:Schema(
        description = "Description of the project",
        example = "Uni bring the information of the various UPorto services to the tip of students' fingers.",
        type = "string",
        minLength = Constants.Description.minSize,
        maxLength = Constants.Description.maxSize
    )
    val description: String
) : Dto<Project>()
