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

    fun getEventsByCategory(category: String): List<Event> = repository.findAllByCategory(category)

    fun getEventById(eventId: Long): Event = repository.findById(eventId).get()

    fun updateEventById(eventId: Long, dto: EventDto): Event {
        val event = getEventById(eventId)
        val newEvent = dto.update(event)
        return repository.save(newEvent)
    }

    fun deleteEventById(eventId: Long): Map<String, String> {
        repository.findById(eventId).get()
        repository.deleteById(eventId)
        return mapOf()
    }
}
