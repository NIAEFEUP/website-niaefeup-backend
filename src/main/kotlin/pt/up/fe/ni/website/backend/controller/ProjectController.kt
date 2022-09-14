package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.service.ProjectService

@RestController
@RequestMapping("/projects")
class ProjectController(private val service: ProjectService) {

    @GetMapping("/")
    fun getAllProjects() = service.getAllProjects()

    @PostMapping("/new")
    fun createNewProject(@RequestBody project: Project) = service.createProject(project)

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable id: Long) = service.getProjectById(id).orElseThrow()!!

    @DeleteMapping("/{id}")
    fun deleteProjectById(@PathVariable id: Long) = service.deleteProjectById(id)
}
