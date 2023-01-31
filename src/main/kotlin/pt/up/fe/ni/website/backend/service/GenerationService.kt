package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.GenerationDto
import pt.up.fe.ni.website.backend.dto.generations.GetGenerationDto
import pt.up.fe.ni.website.backend.dto.generations.UpdateGenerationDto
import pt.up.fe.ni.website.backend.dto.generations.buildGetGenerationDto
import pt.up.fe.ni.website.backend.model.Generation
import pt.up.fe.ni.website.backend.repository.GenerationRepository

@Service
class GenerationService(private val repository: GenerationRepository) {

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
}
