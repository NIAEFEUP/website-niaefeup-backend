package pt.up.fe.ni.website.backend.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.service.activity.EventService
import pt.up.fe.ni.website.backend.utils.validation.ValidImage

@RestController
@RequestMapping("/events")
@Validated
class EventController(private val service: EventService) {
    @GetMapping
    fun getAllEvents() = service.getAllEvents()

    @GetMapping("/{id:\\d+}")
    fun getEventById(@PathVariable id: Long) = service.getEventById(id)

    @GetMapping("/category/{category}")
    fun getEventsByCategory(@PathVariable category: String) = service.getEventsByCategory(category)

    @GetMapping("/{eventSlug}**")
    fun getEvent(@PathVariable eventSlug: String) = service.getEventBySlug(eventSlug)

    @PostMapping("/new", consumes = ["multipart/form-data"])
    fun createEvent(
        @RequestPart event: EventDto,
        @RequestParam
        @ValidImage
        image: MultipartFile
    ): Event {
        event.imageFile = image
        return service.createEvent(event)
    }

    @DeleteMapping("/{id}")
    fun deleteEventById(@PathVariable id: Long): Map<String, String> {
        service.deleteEventById(id)
        return emptyMap()
    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateEventById(
        @PathVariable id: Long,
        @RequestPart event: EventDto,
        @RequestParam
        @ValidImage
        image: MultipartFile?
    ): Event {
        event.imageFile = image
        return service.updateEventById(id, event)
    }

    @PutMapping("/{idEvent}/addTeamMember/{idAccount}")
    fun addTeamMemberById(
        @PathVariable idEvent: Long,
        @PathVariable idAccount: Long
    ) = service.addTeamMemberById(idEvent, idAccount)

    @PutMapping("/{idEvent}/removeTeamMember/{idAccount}", consumes = ["multipart/form-data"])
    fun removeTeamMemberById(
        @PathVariable idEvent: Long,
        @PathVariable idAccount: Long
    ) = service.removeTeamMemberById(idEvent, idAccount)

    @PutMapping("/{idEvent}/gallery/addPhoto", consumes = ["multipart/form-data"])
    fun addGalleryPhoto(
        @PathVariable idEvent: Long,
        @RequestParam
        @ValidImage
        image: MultipartFile
    ) = service.addGalleryPhoto(idEvent, image)

    @PutMapping("/{idEvent}/gallery/removePhoto")
    fun removeGalleryPhoto(
        @PathVariable idEvent: Long,
        @RequestParam photoUrl: String
    ) = service.removeGalleryPhoto(idEvent, photoUrl)
}
