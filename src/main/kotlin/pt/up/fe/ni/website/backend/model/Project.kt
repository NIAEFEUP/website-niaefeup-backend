package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Entity

@Entity
class Project(
    title: String,
    description: String,
    var isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),

    associatedRoles: List<PerActivityRole> = emptyList(),
    id: Long? = null
) : Activity(title, description, associatedRoles, id)
