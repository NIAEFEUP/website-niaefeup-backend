package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Event
import java.util.*

class EventDto(
    val title: String,
    val description: String,
    val teamMembersIds: List<Long>?,
    val registerUrl: String?,
    val date: Date
) : EntityDto<Event>()
