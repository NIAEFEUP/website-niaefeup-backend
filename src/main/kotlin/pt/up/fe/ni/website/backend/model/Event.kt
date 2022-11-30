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

    id: Long? = null
) : Activity(title, description, id)
