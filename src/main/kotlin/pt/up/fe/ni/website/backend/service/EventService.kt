package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository

@Service
class EventService(
    private val repository: EventRepository,
    private val accountService: AccountService
) : ActivityService<Event>(repository, accountService) {
    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun createEvent(dto: EventDto): Event {
        repository.findBySlug(dto.slug)?.let {
            throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        val event = dto.create()

        dto.teamMembersIds?.forEach {
            val account = accountService.getAccountById(it)
            event.teamMembers.add(account)
        }

        return repository.save(event)
    }

    fun getEventsByCategory(category: String): List<Event> = repository.findAllByCategory(category)

    fun getEventById(eventId: Long): Event = repository.findByIdOrNull(eventId)
        ?: throw NoSuchElementException("event not found with id $eventId")

    fun updateEventById(eventId: Long, dto: EventDto): Event {
        val event = getEventById(eventId)
        val newEvent = dto.update(event)
        newEvent.apply {
            teamMembers.clear()
            dto.teamMembersIds?.forEach {
                val account = accountService.getAccountById(it)
                teamMembers.add(account)
            }
        }
        return repository.save(newEvent)
    }

    fun deleteEventById(eventId: Long): Map<String, String> {
        if (!repository.existsById(eventId)) {
            throw NoSuchElementException("event not found with id $eventId")
        }

        repository.deleteById(eventId)
        return mapOf()
    }
}
