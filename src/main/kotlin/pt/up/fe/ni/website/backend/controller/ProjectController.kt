package pt.up.fe.ni.website.backend.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.entity.ProjectDto
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.service.activity.ProjectService
import pt.up.fe.ni.website.backend.utils.validation.ValidImage

@RestController
@RequestMapping("/projects")
@Validated
class ProjectController(private val service: ProjectService) {

    @GetMapping
    fun getAllProjects() = service.getAllProjects()

    @GetMapping("/{id:\\d+}")
    fun getProjectById(@PathVariable id: Long) = service.getProjectById(id)

    @GetMapping("/{projectSlug}**")
    fun getProjectBySlug(@PathVariable projectSlug: String) = service.getProjectBySlug(projectSlug)

    @PostMapping(consumes = ["multipart/form-data"])
    fun createProject(
        @RequestPart project: ProjectDto,
        @RequestParam
        @ValidImage
        image: MultipartFile
    ): Project {
        project.imageFile = image
        return service.createProject(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProjectById(@PathVariable id: Long): Map<String, String> {
        service.deleteProjectById(id)
        return emptyMap()
    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateProjectById(
        @PathVariable id: Long,
        @RequestPart project: ProjectDto,
        @RequestParam
        @ValidImage
        image: MultipartFile?
    ): Project {
        project.imageFile = image
        return service.updateProjectById(id, project)
    }

    @PutMapping("/{id}/archive")
    fun archiveProjectById(@PathVariable id: Long) = service.archiveProjectById(id)

    @PutMapping("/{id}/unarchive")
    fun unarchiveProjectById(@PathVariable id: Long) = service.unarchiveProjectById(id)

    @PutMapping("/{idProject}/team/{idAccount}")
    fun addTeamMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.addTeamMemberById(idProject, idAccount)

    @DeleteMapping("/{idProject}/team/{idAccount}")
    fun removeTeamMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.removeTeamMemberById(idProject, idAccount)

    @PutMapping("/{idProject}/hallOfFame/{idAccount}")
    fun addHallOfFameMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.addHallOfFameMemberById(idProject, idAccount)

    @DeleteMapping("/{idProject}/hallOfFame/{idAccount}")
    fun removeHallOfFameMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.removeHallOfFameMemberById(idProject, idAccount)
}
