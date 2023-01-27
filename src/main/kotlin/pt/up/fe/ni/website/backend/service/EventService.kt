package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository



@Service
class EventService(private val repository: EventRepository, private val accountService: AccountService) {
    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun getEventById(id: Long): Event = repository.findByIdOrNull(id)
        ?: throw throw NoSuchElementException(ErrorMessages.eventNotFound(id))

    fun createEvent(dto: EventDto): Event {
        val event = dto.create()
        return repository.save(event)
    }
}
