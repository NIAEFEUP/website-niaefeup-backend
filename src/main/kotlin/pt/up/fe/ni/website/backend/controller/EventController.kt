package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.service.activity.EventService

@RestController
@RequestMapping("/events")
class EventController(private val service: EventService) {
    @GetMapping
    fun getAllEvents() = service.getAllEvents()

    @GetMapping("/{id:\\d+}")
    fun getEventById(@PathVariable id: Long) = service.getEventById(id)

    @GetMapping("/category/{category}")
    fun getEventsByCategory(@PathVariable category: String) = service.getEventsByCategory(category)

    @GetMapping("/{eventSlug}**")
    fun getEvent(@PathVariable eventSlug: String) = service.getEventBySlug(eventSlug)

    @PostMapping("/new")
    fun createEvent(@RequestBody dto: EventDto) = service.createEvent(dto)

    @DeleteMapping("/{id}")
    fun deleteEventById(@PathVariable id: Long) = service.deleteEventById(id)

    @PutMapping("/{id}")
    fun updateEventById(
        @PathVariable id: Long,
        @RequestBody dto: EventDto,
    ) = service.updateEventById(id, dto)

    @PutMapping("/{idEvent}/addTeamMember/{idAccount}")
    fun addTeamMemberById(
        @PathVariable idEvent: Long,
        @PathVariable idAccount: Long,
    ) = service.addTeamMemberById(idEvent, idAccount)

    @PutMapping("/{idEvent}/removeTeamMember/{idAccount}")
    fun removeTeamMemberById(
        @PathVariable idEvent: Long,
        @PathVariable idAccount: Long,
    ) = service.removeTeamMemberById(idEvent, idAccount)
}
