package pt.up.fe.ni.website.backend.service

import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import java.util.Optional

@Service
class ProjectService(private val repository: ProjectRepository) {

    fun getAllProjects() = repository.findAll().toList()

    fun createProject(project: Project) = repository.save(project)

    fun getProjectById(id: Long): Optional<Project> = repository.findById(id)

    fun deleteProjectById(id: Long) = repository.deleteById(id)
}
