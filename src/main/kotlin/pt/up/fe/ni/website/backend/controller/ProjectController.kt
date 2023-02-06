package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.ProjectDto
import pt.up.fe.ni.website.backend.service.ProjectService

@RestController
@RequestMapping("/projects")
class ProjectController(private val service: ProjectService) {

    @GetMapping
    fun getAllProjects() = service.getAllProjects()

    @GetMapping("/{id:\\d+}")
    fun getProjectById(@PathVariable id: Long) = service.getProjectById(id)

    @GetMapping("/{projectSlug}**")
    fun getProjectBySlug(@PathVariable projectSlug: String) = service.getProjectBySlug(projectSlug)

    @PostMapping("/new")
    fun createNewProject(@RequestBody dto: ProjectDto) = service.createProject(dto)

    @DeleteMapping("/{id}")
    fun deleteProjectById(@PathVariable id: Long): Map<String, String> {
        service.deleteProjectById(id)
        return emptyMap()
    }

    @PutMapping("/{id}")
    fun updatePostById(
        @PathVariable id: Long,
        @RequestBody dto: ProjectDto
    ) = service.updateProjectById(id, dto)

    @PutMapping("/{id}/archive")
    fun archiveProjectById(@PathVariable id: Long) = service.archiveProjectById(id)

    @PutMapping("/{id}/unarchive")
    fun unarchiveProjectById(@PathVariable id: Long) = service.unarchiveProjectById(id)

    @PutMapping("/{idProject}/addTeamMember/{idAccount}")
    fun addTeamMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.addTeamMemberById(idProject, idAccount)

    @PutMapping("/{idProject}/removeTeamMember/{idAccount}")
    fun removeTeamMemberById(
        @PathVariable idProject: Long,
        @PathVariable idAccount: Long
    ) = service.removeTeamMemberById(idProject, idAccount)
}
