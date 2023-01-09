package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import java.util.Date

@Entity
class Event(
    title: String,
    description: String,

    @field:NullOrNotBlank
    @field:URL
    var registerUrl: String? = null,

    @JsonProperty(required = true)
    val date: Date,

    id: Long? = null
) : Activity(title, description, id)
