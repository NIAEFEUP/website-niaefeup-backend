package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import pt.up.fe.ni.website.backend.dto.entity.AccountDto
import pt.up.fe.ni.website.backend.dto.entity.EventDto
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
