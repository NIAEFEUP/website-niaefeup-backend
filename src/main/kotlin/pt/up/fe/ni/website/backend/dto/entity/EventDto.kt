package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.model.Event
import java.util.Date

class EventDto(
    val title: String,
    val description: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val teamMembers: MutableList<AccountDto>,
    val registerUrl: String?,
    val date: Date
) : EntityDto<Event>()
