package pt.up.fe.ni.website.backend.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.constants.EventConstants
import java.util.Date

@Schema(description = "Event model")
class EventDto(
        @field:Schema(
                description = "Title of the event",
                example = "Workshop C++",
                type = "string",
                minLength = EventConstants.Title.minSize,
                maxLength = EventConstants.Title.maxSize
        )
        val title: String,
        @field:Schema(
                description = "Description of the event",
                example = "This is a workshop prepared by NIAEFEUP to help new learners to get to know the language.",
                type = "string",
                minLength = EventConstants.Description.minSize,
                maxLength = EventConstants.Description.maxSize
        )
        val description: String,
        @field:Schema(
                description = "Date of the event",
                example = "2022-05-12T14:00:00",
                type = "string"
        )
        val date: Date
) : Dto<Event>()
