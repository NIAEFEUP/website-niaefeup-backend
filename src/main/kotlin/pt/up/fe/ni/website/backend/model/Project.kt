package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Entity

@Entity
class Project(
    title: String,
    description: String,
    var isArchived: Boolean = false,
    technologies: List<String>,
    id: Long? = null
) : Activity(title, description, id)
