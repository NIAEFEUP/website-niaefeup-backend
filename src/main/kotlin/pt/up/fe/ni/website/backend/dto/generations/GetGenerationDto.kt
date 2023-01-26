package pt.up.fe.ni.website.backend.dto.generations

import pt.up.fe.ni.website.backend.model.Generation

typealias GetGenerationDto = List<GenerationSectionDto>

data class GenerationUserDto(
    val name: String,
    val roles: List<String>
)

data class GenerationSectionDto(
    val section: String,
    val users: List<GenerationUserDto>
)

fun buildGetGenerationDto(generation: Generation): GetGenerationDto {
    val sections = generation.roles
        .filter { it.isSection && it.accounts.isNotEmpty() }
        .map { role ->
            GenerationSectionDto(
                section = role.name,
                users = role.accounts.map { account ->
                    GenerationUserDto(
                        name = account.name,
                        roles = account.roles
                            .filter { it.generation == generation && !it.isSection }
                            .map { it.name }
                    )
                }
            )
        }
    return sections
}
