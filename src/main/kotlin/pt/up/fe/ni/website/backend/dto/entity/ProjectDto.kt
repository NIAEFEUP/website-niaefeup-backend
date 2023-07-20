package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.model.Project

class ProjectDto(
    val title: String,
    val description: String,
    var hallOfFameIds: List<Long>?,
    val teamMembersIds: List<Long>?,
    val isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    val slug: String?,
    val slogan: String?,
    val targetAudience: String,
    val links: List<CustomWebsiteDto>?,
    var image: String?,
    @JsonIgnore
    var imageFile: MultipartFile?
) : EntityDto<Project>()
