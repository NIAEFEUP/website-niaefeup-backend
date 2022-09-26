package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.dto.ProjectDto
import pt.up.fe.ni.website.backend.service.ProjectService

@RestController
@RequestMapping("/projects")
class ProjectController(private val service: ProjectService) {

    @GetMapping
    fun getAllProjects() = service.getAllProjects()

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable id: Long) = service.getProjectById(id).orElseThrow()!!

    @PostMapping("/new")
    fun createNewProject(@RequestBody dto: ProjectDto): Project {
        val project = dto.update(Project.BLANK_PROJECT)
        return service.saveProject(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProjectById(@PathVariable id: Long) = service.deleteProjectById(id)

    @PutMapping("/{id}")
    fun updatePostById(
        @PathVariable id: Long,
        @RequestBody dto: ProjectDto
    ): Project {
        val project = service.getProjectById(id).orElseThrow()!!
        val newProject = dto.update(project)
        return service.saveProject(newProject)
    }
}
