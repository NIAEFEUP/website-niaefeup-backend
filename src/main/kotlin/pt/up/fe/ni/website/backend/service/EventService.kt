package pt.up.fe.ni.website.backend.service

import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository

@Service
class EventService(private val repository: EventRepository) {
    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun createEvent(dto: EventDto): Event {
        val event = dto.create()
        return repository.save(event)
    }
}
