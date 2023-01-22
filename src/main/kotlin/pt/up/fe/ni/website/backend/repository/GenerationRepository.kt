package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Generation

@Repository
interface GenerationRepository : CrudRepository<Generation, Long> {
    fun findBySchoolYear(schoolYear: String): Generation?
}
