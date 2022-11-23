package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import pt.up.fe.ni.website.backend.model.Event

interface EventRepository : CrudRepository<Event, Long> {
    fun findAllByCategory(category: String): List<Event>
}
