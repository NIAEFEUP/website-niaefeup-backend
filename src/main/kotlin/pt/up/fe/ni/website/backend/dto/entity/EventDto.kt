package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Event
import java.util.Date

class EventDto(
    val title: String,
    val description: String,
    val registerUrl: String?,
    val date: Date,
    val teamMembers: MutableList<AccountDto>
) : EntityDto<Event>()
