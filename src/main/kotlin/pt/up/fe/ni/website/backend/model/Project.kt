package pt.up.fe.ni.website.backend.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Project(
    var name: String,
    var description: String,
    @Id @GeneratedValue
    val id: Long? = null
)
