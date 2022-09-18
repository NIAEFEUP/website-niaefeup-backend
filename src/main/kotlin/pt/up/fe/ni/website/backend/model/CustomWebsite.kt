package pt.up.fe.ni.website.backend.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class CustomWebsite(
    val url: String,
    val icon: String?,
    @Id
    @GeneratedValue
    val id: Long? = null
)