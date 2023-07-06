package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Activity

@Repository
interface ActivityRepository<T : Activity> : CrudRepository<T, Long> {
    fun findBySlug(slug: String?): T?
    fun findByTitle(title: String?): T?
}
