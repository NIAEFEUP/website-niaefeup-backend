package pt.up.fe.ni.website.backend.model

import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Post(
    var description: String,
    var link: String,
    var date: Date,
    @Id @GeneratedValue val id: Long? = null
)
