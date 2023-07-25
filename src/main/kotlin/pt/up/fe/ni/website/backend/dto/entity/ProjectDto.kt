package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    title: String,
    description: String,
    teamMembersIds: List<Long>?,
    slug: String?,
    image: String?,

    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    val slogan: String?,
    val targetAudience: String,
    val links: List<CustomWebsiteDto>?,
    val timeline: List<TimelineEventDto>?,
    val hallOfFameIds: List<Long>?,
) : ActivityDto<Project>(title, description, teamMembersIds, slug, image)
