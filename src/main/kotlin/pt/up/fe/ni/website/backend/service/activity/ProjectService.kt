package pt.up.fe.ni.website.backend.service.activity

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.ProjectDto
import pt.up.fe.ni.website.backend.model.Project
import pt.up.fe.ni.website.backend.repository.ProjectRepository
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.service.ErrorMessages
import pt.up.fe.ni.website.backend.service.upload.FileUploader

@Service
class ProjectService(
    override val repository: ProjectRepository,
    accountService: AccountService,
    fileUploader: FileUploader
) : AbstractActivityService<Project>(repository, accountService, fileUploader) {

    companion object {
        const val IMAGE_FOLDER = "projects"
    }

    fun getAllProjects(): List<Project> = repository.findAll().toList()

    fun getProjectById(id: Long): Project = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException(ErrorMessages.projectNotFound(id))

    fun getProjectBySlug(projectSlug: String): Project = repository.findBySlug(projectSlug)
        ?: throw NoSuchElementException(ErrorMessages.projectNotFound(projectSlug))

    fun createProject(dto: ProjectDto): Project {
        val project = createActivity(dto, IMAGE_FOLDER)
        dto.hallOfFameIds?.forEach {
            val account = accountService.getAccountById(it)
            project.hallOfFame.add(account)
        }
        return repository.save(project)
    }

    fun updateProjectById(id: Long, dto: ProjectDto): Project {
        val project = getProjectById(id)
        val newProject = updateActivityById(project, dto, IMAGE_FOLDER)
        newProject.apply {
            hallOfFame.clear()
            dto.hallOfFameIds?.forEach {
                val account = accountService.getAccountById(it)
                hallOfFame.add(account)
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

    fun addHallOfFameMemberById(idProject: Long, idAccount: Long): Project {
        val project = getProjectById(idProject)
        val account = accountService.getAccountById(idAccount)
        project.hallOfFame.add(account)
        return repository.save(project)
    }

    fun removeHallOfFameMemberById(idProject: Long, idAccount: Long): Project {
        val project = getProjectById(idProject)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(
                ErrorMessages.accountNotFound(
                    idAccount
                )
            )
        }
        project.hallOfFame.removeIf { it.id == idAccount }
        return repository.save(project)
    }
}
