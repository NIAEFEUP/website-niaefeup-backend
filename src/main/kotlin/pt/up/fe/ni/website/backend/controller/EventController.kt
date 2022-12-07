package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.*
import pt.up.fe.ni.website.backend.model.dto.AccountDto
import pt.up.fe.ni.website.backend.model.dto.EventDto
import pt.up.fe.ni.website.backend.service.EventService

@RestController
@RequestMapping("/events")
class EventController(private val service: EventService) {
    @GetMapping
    fun getAllEvents() = service.getAllEvents()

    @PostMapping("/new")
    fun createEvent(@RequestBody dto: EventDto) = service.createEvent(dto)

    @PutMapping("/{id}/addTeamMember")
    fun addTeamMemberById(
        @PathVariable id: Long,
        @RequestBody account: AccountDto
    ) = service.addTeamMemberById(id, account)
}
