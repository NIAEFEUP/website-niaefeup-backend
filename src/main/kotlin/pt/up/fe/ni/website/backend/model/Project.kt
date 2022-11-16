package pt.up.fe.ni.website.backend.model

import javax.persistence.Entity

@Entity
class Project(
    override val title: String,
    override val description: String,
    override val id: Long? = null,
    var isArchived: Boolean = false,
) : Activity(title, description, id)
