package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval

class EventDto(
    title: String,
    description: String,
    teamMembersIds: List<Long>?,
    slug: String?,
    image: String?,

    val registerUrl: String?,
    val dateInterval: DateInterval,
    val location: String?,
    val category: String?
) : ActivityDto<Event>(title, description, teamMembersIds, slug, image)
