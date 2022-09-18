package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size

@Entity
class Event(
    @JsonProperty(required = true)
    @field:Size(min = 2, max = 500)
    val title: String,

    @JsonProperty(required = true)
    @field:Size(min = 10, max = 10000)
    val description: String,

    @JsonProperty(required = true)
    val date: Date,

    @Id @GeneratedValue
    val id: Long? = null
)
