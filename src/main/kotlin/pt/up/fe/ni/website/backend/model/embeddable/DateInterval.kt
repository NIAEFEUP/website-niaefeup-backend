package pt.up.fe.ni.website.backend.model.embeddable

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embeddable
import java.util.Date
import pt.up.fe.ni.website.backend.annotations.validation.ValidDateInterval

@ValidDateInterval
@Embeddable
class DateInterval(
    @JsonProperty(required = true)
    val startDate: Date,
    val endDate: Date? = null
)
