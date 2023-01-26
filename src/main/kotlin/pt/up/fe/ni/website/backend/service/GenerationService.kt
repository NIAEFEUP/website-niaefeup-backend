package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.generations.GetGenerationDto
import pt.up.fe.ni.website.backend.dto.generations.buildGetGenerationDto
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
        val generation =
            repository.findFirstByOrderBySchoolYearDesc()
                ?: throw NoSuchElementException(ErrorMessages.noGenerations)
        return buildGetGenerationDto(generation)
    }
}
