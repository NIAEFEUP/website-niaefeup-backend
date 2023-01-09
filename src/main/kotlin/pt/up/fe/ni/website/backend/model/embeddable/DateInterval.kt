package pt.up.fe.ni.website.backend.model.embeddable

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embeddable
import pt.up.fe.ni.website.backend.annotations.validation.DateInterval
import java.util.Date

@DateInterval(startDate = "startDate", endDate = "endDate")
@Embeddable
class DateInterval(
    @JsonProperty(required = true)
    val startDate: Date,
    val endDate: Date?
)
