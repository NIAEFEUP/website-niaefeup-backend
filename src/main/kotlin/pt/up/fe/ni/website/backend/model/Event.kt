package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import javax.persistence.Entity

@Entity
class Event(
    title: String,
    description: String,

    @JsonProperty(required = true)
    val date: Date,

    teamMembers: MutableList<Account>,
    id: Long? = null
) : Activity(title, description, teamMembers, id)
