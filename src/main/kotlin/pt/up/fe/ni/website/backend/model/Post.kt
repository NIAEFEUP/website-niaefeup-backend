package pt.up.fe.ni.website.backend.model

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Post (
        val description: String,
        val link: String,
        val date: Date,
        @Id @GeneratedValue val id: Long? = null
)
