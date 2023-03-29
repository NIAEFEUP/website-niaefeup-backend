package pt.up.fe.ni.website.backend.dto.generations

import pt.up.fe.ni.website.backend.utils.validation.SchoolYear

data class UpdateGenerationDto(
    @SchoolYear
    val schoolYear: String
)
