package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import pt.up.fe.ni.website.backend.model.constants.EventConstants
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
    @field:Schema(
            description = "Title of the event",
            example = "Workshop C++",
            type = "string",
            minLength = EventConstants.Title.minSize,
            maxLength = EventConstants.Title.maxSize
    )
    val title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    @field:Schema(
            description = "Description of the event",
            example = "This is a workshop prepared by NIAEFEUP to help new learners to get to know the language.",
            type = "string",
            minLength = EventConstants.Description.minSize,
            maxLength = EventConstants.Description.maxSize
    )
    val description: String,

    @JsonProperty(required = true)
    @field:Schema(
            description = "Date of the event",
            example = "2022-05-12T14:00:00",
            type = "string"
    )
    val date: Date,

    @Id @GeneratedValue
    @field:Schema(
            description = "ID of the event",
            example = "2",
            type = "integer",
            format = "int64"
    )
    val id: Long? = null
)
