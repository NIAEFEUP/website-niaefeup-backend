package pt.up.fe.ni.website.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.dto.EventDto
import pt.up.fe.ni.website.backend.service.EventService

@RestController
@Tag(name = "Events", description = "Event related endpoints")
@RequestMapping("/events")
class EventController(private val service: EventService) {
    @Operation(summary = "Gets all events", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful Operation",
                content = arrayOf(
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = Event::class))
                    )
                )
            )
        ]
    )
    @GetMapping
    fun getAllEvents() = service.getAllEvents()

    @Operation(summary = "Creates a new event", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Event::class))))
        ]
    )
    @PostMapping("/new")
    fun createEvent(@RequestBody dto: EventDto) = service.createEvent(dto)
}
