package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.*
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.service.EventService
import javax.validation.Valid

@RestController
@RequestMapping("/events")
class EventController(private val service: EventService) {
    @GetMapping
    fun getAllEvents() = service.getAllEvents()

    @PostMapping("/new")
    fun createEvent(@Valid @RequestBody event: Event) = service.createEvent(event)
}
