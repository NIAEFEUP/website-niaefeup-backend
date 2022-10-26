package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.model.dto.ProjectDto
import pt.up.fe.ni.website.backend.repository.ProjectRepository

@Service
class ProjectService(private val repository: ProjectRepository) {

    fun getAllProjects(): List<Project> = repository.findAll().toList()

    fun createProject(dto: ProjectDto): Project {
        val project = dto.create()
        return repository.save(project)
    }

    fun getProjectById(id: Long): Project = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException("project not found with id $id")

    fun updateProjectById(id: Long, dto: ProjectDto): Project {
        val project = getProjectById(id)
        val newProject = dto.update(project)
        return repository.save(newProject)
    }

    fun deleteProjectById(id: Long): Map<String, String> {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("project not found with id $id")
        }

        repository.deleteById(id)
        return emptyMap()
    }
}
