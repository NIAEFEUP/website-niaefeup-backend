package pt.up.fe.ni.website.backend.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.GenerationDto
import pt.up.fe.ni.website.backend.dto.generations.GetGenerationDto
import pt.up.fe.ni.website.backend.dto.generations.UpdateGenerationDto
import pt.up.fe.ni.website.backend.dto.generations.buildGetGenerationDto
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.repository.GenerationRepository

@Service
@Transactional
class GenerationService(
    private val repository: GenerationRepository,
    private val accountService: AccountService,
    private val projectService: ProjectService
) {

    fun getAllGenerations(): List<String> = repository.findAllSchoolYear()

    fun getGenerationById(id: Long): GetGenerationDto {
        val generation =
            repository.findByIdOrNull(id) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(id))
        return buildGetGenerationDto(generation)
    }

    fun getGenerationByYear(year: String): GetGenerationDto {
        val generation =
            repository.findBySchoolYear(year) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(year))
        return buildGetGenerationDto(generation)
    }

    fun getLatestGeneration(): GetGenerationDto {
        val generation = repository.findFirstByOrderBySchoolYearDesc()
            ?: throw NoSuchElementException(ErrorMessages.noGenerations)
        return buildGetGenerationDto(generation)
    }

    fun createNewGeneration(dto: GenerationDto): Generation {
        repository.findBySchoolYear(dto.schoolYear)?.let {
            throw IllegalArgumentException(ErrorMessages.generationAlreadyExists)
        }

        val generation = dto.create()
        generation.roles.forEachIndexed { roleIdx, role ->
            val roleDto = dto.roles[roleIdx]

            roleDto.accountIds.forEach {
                val account = accountService.getAccountById(it)
                role.accounts.add(account)
            }

            roleDto.associatedActivities.forEachIndexed associatedLoop@{ activityRoleIdx, activityRoleDto ->
                val activityRole = role.associatedActivities[activityRoleIdx]
                val activityId = activityRoleDto.activityId ?: return@associatedLoop
                // TODO: Use activity service once PR is merged
                activityRole.activity = projectService.getProjectById(activityId)
            }
        }

        return repository.save(generation)
    }

    fun updateGenerationById(id: Long, dto: UpdateGenerationDto): Generation {
        val generation =
            repository.findByIdOrNull(id) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(id))
        return updateGenerationByYear(generation.schoolYear, dto)
    }

    fun updateGenerationByYear(year: String, dto: UpdateGenerationDto): Generation {
        val generation =
            repository.findBySchoolYear(year) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(year))

        repository.findBySchoolYear(dto.schoolYear)?.let {
            throw IllegalArgumentException(ErrorMessages.generationAlreadyExists)
        }

        generation.schoolYear = dto.schoolYear
        return repository.save(generation)
    }

    fun deleteGenerationByYear(year: String) {
        val generation =
            repository.findBySchoolYear(year) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(year))
        repository.delete(generation)
    }

    fun deleteGenerationById(id: Long) {
        repository.findByIdOrNull(id) ?: throw NoSuchElementException(ErrorMessages.generationNotFound(id))
        repository.deleteById(id)
    }
}
