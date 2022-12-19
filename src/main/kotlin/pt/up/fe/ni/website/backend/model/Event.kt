package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import java.util.Date

@Entity
class Event(
    title: String,
    description: String,

    @JsonProperty(required = true)
    val date: Date,

    id: Long? = null
) : Activity(title, description, id)
