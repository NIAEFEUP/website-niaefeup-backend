package pt.up.fe.ni.website.backend.model

import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Event(
    val title: String,
    val description: String,
    val date: Date,
    @Id @GeneratedValue val id: Long? = null
)
