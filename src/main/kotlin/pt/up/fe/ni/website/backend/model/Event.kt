package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.DateInterval
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

@Entity
@DateInterval(startDate = "startDate", endDate = "endDate")
class Event(
    title: String,
    description: String,

    @field:NullOrNotBlank
    @field:URL
    var registerUrl: String? = null,

    @JsonProperty(required = true)
    val startDate: Date,

    val endDate: Date?,

    @field:Size(min = Constants.Location.minSize, max = Constants.Location.maxSize)
    val location: String?,

    @field:Size(min = Constants.Category.minSize, max = Constants.Category.maxSize)
    val category: String?,

    @JsonProperty(required = true)
    @field:NotEmpty
    @field:URL
    val thumbnailPath: String,

    id: Long? = null
) : Activity(title, description, id)
