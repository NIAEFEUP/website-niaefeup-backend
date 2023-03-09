package pt.up.fe.ni.website.backend.service.activity

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.ProjectDto
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.service.ErrorMessages

@Service
class ProjectService(
    override val repository: ProjectRepository,
    accountService: AccountService
) : AbstractActivityService<Project>(repository, accountService) {

    fun getAllProjects(): List<Project> = repository.findAll().toList()

    fun createProject(dto: ProjectDto): Project {
        repository.findBySlug(dto.slug)?.let {
            throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        val project = dto.create()

        dto.teamMembersIds?.forEach {
            val account = accountService.getAccountById(it)
            project.teamMembers.add(account)
        }

        return repository.save(project)
    }

    fun getProjectById(id: Long): Project = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.projectNotFound(id))

    fun getProjectBySlug(projectSlug: String): Project = repository.findBySlug(projectSlug)
        ?: throw NoSuchElementException(ErrorMessages.projectNotFound(projectSlug))

    fun updateProjectById(id: Long, dto: ProjectDto): Project {
        val project = getProjectById(id)

        repository.findBySlug(dto.slug)?.let {
            if (it.id != project.id) throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        val newProject = dto.update(project)
        newProject.apply {
            teamMembers.clear()
            dto.teamMembersIds?.forEach {
                val account = accountService.getAccountById(it)
                teamMembers.add(account)
            }
        }
        return repository.save(newProject)
    }

    fun deleteProjectById(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException(ErrorMessages.projectNotFound(id))
        }

        repository.deleteById(id)
    }

    fun archiveProjectById(id: Long): Project {
        val project = getProjectById(id)
        project.isArchived = true
        return repository.save(project)
    }

    fun unarchiveProjectById(id: Long): Project {
        val project = getProjectById(id)
        project.isArchived = false
        return repository.save(project)
    }
}
