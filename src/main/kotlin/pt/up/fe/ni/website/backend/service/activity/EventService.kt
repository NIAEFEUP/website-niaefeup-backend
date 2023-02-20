package pt.up.fe.ni.website.backend.service.activity

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.service.ErrorMessages

@Service
class EventService(
    override val repository: EventRepository,
    accountService: AccountService,
) : AbstractActivityService<Event>(repository, accountService) {
    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun getEventBySlug(eventSlug: String): Event =
        repository.findBySlug(eventSlug) ?: throw NoSuchElementException(ErrorMessages.eventNotFound(eventSlug))

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
        ?: throw NoSuchElementException(ErrorMessages.eventNotFound(eventId))

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
            throw NoSuchElementException(ErrorMessages.eventNotFound(eventId))
        }

        repository.deleteById(eventId)
        return mapOf()
    }
}
