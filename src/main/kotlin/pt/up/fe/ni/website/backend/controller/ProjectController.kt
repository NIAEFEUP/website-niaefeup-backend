package pt.up.fe.ni.website.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Projects", description = "Project related endpoints")
@RequestMapping("/projects")
class ProjectController(private val service: ProjectService) {

    @Operation(summary = "Gets all projects", description = "Returns the list off all projects in the content")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = Project::class)))))])
    @GetMapping
    fun getAllProjects() = service.getAllProjects()

    @Operation(summary = "Gets a project by id", description = "Returns a single project")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Project::class))))])
    @GetMapping("/{id}")
    fun getProjectById(@PathVariable id: Long) = service.getProjectById(id)

    @Operation(summary = "Creates a new project", description = "Returns the newly created project")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Project::class))))])
    @PostMapping("/new")
    fun createNewProject(@RequestBody dto: ProjectDto) = service.createProject(dto)

    @Operation(summary = "Deletes a project by id", description = "Returns an empty map")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Map::class))))])
    @DeleteMapping("/{id}")
    fun deleteProjectById(@PathVariable id: Long) = service.deleteProjectById(id)

    @Operation(summary = "Updates a project by id", description = "Returns the updated project")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Project::class))))])
    @PutMapping("/{id}")
    fun updatePostById(
        @PathVariable id: Long,
        @RequestBody dto: ProjectDto
    ) = service.updateProjectById(id, dto)
}
