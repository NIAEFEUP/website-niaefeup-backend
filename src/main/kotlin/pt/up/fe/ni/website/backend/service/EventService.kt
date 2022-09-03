package pt.up.fe.ni.website.backend.service

import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository

@Service
class EventService(private val repository: EventRepository) {
    fun getAllEvents(): Collection<Event> = repository.findAll().toList()

    fun createEvent(event: Event) = repository.save(event)
}
