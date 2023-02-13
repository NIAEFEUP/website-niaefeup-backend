package pt.up.fe.ni.website.backend.model.embeddable

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embeddable
import pt.up.fe.ni.website.backend.annotations.validation.ValidDateInterval
import java.util.Date

@ValidDateInterval
@Embeddable
class DateInterval(
    @JsonProperty(required = true)
    val startDate: Date,
    val endDate: Date?
)
