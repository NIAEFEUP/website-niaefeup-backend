package pt.up.fe.ni.website.backend.service.activity

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.entity.EventDto
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.service.ErrorMessages
import pt.up.fe.ni.website.backend.service.upload.FileUploader

@Service
class EventService(
    override val repository: EventRepository,
    accountService: AccountService,
    fileUploader: FileUploader
) : AbstractActivityService<Event>(repository, accountService, fileUploader) {

    companion object {
        const val IMAGE_FOLDER = "events"
    }

    fun getAllEvents(): List<Event> = repository.findAll().toList()

    fun getEventsByCategory(category: String): List<Event> = repository.findAllByCategory(category)

    fun getEventById(eventId: Long): Event = repository.findByIdOrNull(eventId)
        ?: throw NoSuchElementException(ErrorMessages.eventNotFound(eventId))

    fun getEventBySlug(eventSlug: String): Event =
        repository.findBySlug(eventSlug) ?: throw NoSuchElementException(ErrorMessages.eventNotFound(eventSlug))

    fun createEvent(dto: EventDto) = createActivity(dto, IMAGE_FOLDER)

    fun updateEventById(eventId: Long, dto: EventDto): Event {
        val event = getEventById(eventId)
        return updateActivityById(event, dto, IMAGE_FOLDER)
    }

    fun deleteEventById(eventId: Long) {
        if (!repository.existsById(eventId)) {
            throw NoSuchElementException(ErrorMessages.eventNotFound(eventId))
        }

        repository.deleteById(eventId)
    }

    fun addGalleryPhoto(eventId: Long, image: MultipartFile): Event {
        val event = getEventById(eventId)

        val fileName = fileUploader.buildFileName(image, event.title)
        val imageName = fileUploader.uploadImage("gallery", fileName, image.bytes)

        event.gallery.add(imageName)

        return repository.save(event)
    }

    fun removeGalleryPhoto(eventId: Long, photoName: String) {
        val event = getEventById(eventId)

        event.gallery.remove(photoName)

        repository.save(event)
    }
}
