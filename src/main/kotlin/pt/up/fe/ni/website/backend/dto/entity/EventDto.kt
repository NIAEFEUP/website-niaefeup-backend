package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Event
import java.util.Date

class EventDto(
    val title: String,
    val description: String,
    val registerUrl: String?,
    val startDate: Date,
    val endDate: Date?,
    val url: String?,
    val location: String?,
    val category: String?,
    val thumbnailPath: String
) : EntityDto<Event>()
