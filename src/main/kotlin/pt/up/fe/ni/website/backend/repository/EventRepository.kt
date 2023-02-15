package pt.up.fe.ni.website.backend.repository

import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Event

@Repository
interface EventRepository : ActivityRepository<Event> {

    fun findAllByCategory(category: String): List<Event>
}
