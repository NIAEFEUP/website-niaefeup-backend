package pt.up.fe.ni.website.backend.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Generation

@Repository
interface GenerationRepository : CrudRepository<Generation, Long> {
    fun findBySchoolYear(schoolYear: String): Generation?

    fun findFirstByOrderBySchoolYearDesc(): Generation?

    @Query("SELECT schoolYear FROM Generation")
    fun findAllSchoolYear(): List<String>
}
