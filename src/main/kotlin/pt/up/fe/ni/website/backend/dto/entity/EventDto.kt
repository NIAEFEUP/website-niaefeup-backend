package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval

class EventDto(
    val title: String,
    val description: String,
    val registerUrl: String?,
    val dateInterval: DateInterval,
    val location: String?,
    val category: String?,
    val thumbnailPath: String
) : EntityDto<Event>()
