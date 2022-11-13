package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

@Entity
class Event(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize)
    val title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    val description: String,

    @JsonProperty(required = true)
    val date: Date,

    @Id @GeneratedValue
    val id: Long? = null
)
