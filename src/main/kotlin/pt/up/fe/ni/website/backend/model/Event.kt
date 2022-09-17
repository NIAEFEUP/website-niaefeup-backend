package pt.up.fe.ni.website.backend.model

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size

@Entity
class Event(
    @field:Size(min = 2, max = 500)
    val title: String,

    @field:Size(min = 10, max = 10000)
    val description: String,

    val date: Date,

    @Id @GeneratedValue
    val id: Long? = null
)
