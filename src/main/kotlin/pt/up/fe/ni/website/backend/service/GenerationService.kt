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
import pt.up.fe.ni.website.backend.service.activity.ActivityService

@Service
@Transactional
class GenerationService(
    private val repository: GenerationRepository,
    private val accountService: AccountService,
    private val activityService: ActivityService
) {

    fun getAllGenerations(): List<String> = repository.findAllSchoolYearOrdered()

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
        dto.schoolYear = inferSchoolYearIfNotSpecified(dto)
        val generation = dto.create()
        assignRolesAndActivities(generation, dto)
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

    private fun inferSchoolYearIfNotSpecified(dto: GenerationDto): String {
        dto.schoolYear?.let {
            repository.findBySchoolYear(it)?.let {
                throw IllegalArgumentException(ErrorMessages.generationAlreadyExists)
            }
            return it
        }

        val lastSchoolYear = repository.findFirstByOrderBySchoolYearDesc()?.schoolYear
            ?: throw IllegalArgumentException(ErrorMessages.noGenerationsToInferYear)
        val lastYear = lastSchoolYear.substring(3, 5).toInt()
        return "$lastYear-${lastYear + 1}"
    }

    private fun assignRolesAndActivities(generation: Generation, dto: GenerationDto) {
        generation.roles.forEachIndexed { roleIdx, role ->
            val roleDto = dto.roles[roleIdx]

            roleDto.accountIds.forEach {
                val account = accountService.getAccountById(it)

                // only owner side is needed after transaction, but it's useful to update the objects
                account.roles.add(role)
                role.accounts.add(account)
            }

            roleDto.associatedActivities.forEachIndexed associatedLoop@{ activityRoleIdx, activityRoleDto ->
                val perActivityRole = role.associatedActivities[activityRoleIdx]
                val activityId = activityRoleDto.activityId ?: return@associatedLoop
                val activity = activityService.getActivityById(activityId)

                // only owner side is needed after transaction, but it's useful to update the objects
                perActivityRole.activity = activity
                activity.associatedRoles.add(perActivityRole)
            }
        }
    }
}
