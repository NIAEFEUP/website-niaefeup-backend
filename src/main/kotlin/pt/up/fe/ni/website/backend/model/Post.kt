package pt.up.fe.ni.website.backend.model

import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Post(
    var title: String,
    var body: String,
    var thumbnailPath: String,
    val publishDate: Date,
    @Id @GeneratedValue val id: Long? = null
)
