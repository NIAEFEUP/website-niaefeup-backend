package pt.up.fe.ni.website.backend.dto.entity

import java.util.Date
import pt.up.fe.ni.website.backend.model.TimelineEvent

class TimelineEventDto(
    val date: Date,
    val description: String
) : EntityDto<TimelineEvent>()
