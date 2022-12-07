package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.dto.entity.AccountDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository

@Service
class EventService(private val repository: EventRepository, private val accountService: AccountService) {
    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun getEventById(id: Long): Event = repository.findByIdOrNull(id)
        ?: throw NoSuchElementException("event not found with id $id")

    fun createEvent(dto: EventDto): Event {
        val event = dto.create()
        return repository.save(event)
    }

    fun addTeamMemberById(idEvent: Long, idAccount: Long): Event {
        val event = getEventById(idEvent)
        val account = accountService.getAccountById(idAccount)
        event.teamMembers.add(account)
        return repository.save(event)
    }

    fun removeTeamMemberById(idEvent: Long, idAccount: Long): Event {
        val event = getEventById(idEvent)
        val account = accountService.getAccountById(idAccount)
        event.teamMembers.remove(account)
        return repository.save(event)
    }
}
