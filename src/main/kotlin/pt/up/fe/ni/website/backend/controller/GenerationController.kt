package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.GenerationDto
import pt.up.fe.ni.website.backend.dto.generations.UpdateGenerationDto
import pt.up.fe.ni.website.backend.service.GenerationService

@RestController
@RequestMapping("/generations")
class GenerationController(private val service: GenerationService) {
    @GetMapping
    fun getAllGenerations() = service.getAllGenerations()

    @GetMapping("/{id:\\d+}")
    fun getGenerationByYear(@PathVariable id: Long) = service.getGenerationById(id)

    @GetMapping("/{year:\\d{2}-\\d{2}}")
    fun getGenerationByYear(@PathVariable year: String) = service.getGenerationByYear(year)

    @GetMapping("/latest")
    fun getLatestGeneration() = service.getLatestGeneration()

    @PostMapping("/new")
    fun createNewGeneration(
        @RequestParam year: String?,
        @RequestBody dto: GenerationDto
    ) = service.createNewGeneration(dto)

    @PatchMapping("/{id:\\d+}")
    fun updateGenerationById(
        @PathVariable id: Long,
        @RequestBody dto: UpdateGenerationDto
    ) = service.updateGenerationById(id, dto)

    @PatchMapping("/{year:\\d{2}-\\d{2}}")
    fun updateGenerationByYear(
        @PathVariable year: String,
        @RequestBody dto: UpdateGenerationDto
    ) = service.updateGenerationByYear(year, dto)
}
