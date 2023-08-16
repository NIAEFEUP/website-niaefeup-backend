package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import java.util.Date

@Entity
class TimelineEvent(
    val date: Date,

    @field:NotEmpty
    val description: String,

    @Id @GeneratedValue
    val id: Long? = null
)
