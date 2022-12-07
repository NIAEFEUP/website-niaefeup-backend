package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Entity

@Entity
class Project(
    title: String,
    description: String,
    teamMembers: MutableList<Account>,
    var isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    id: Long? = null
) : Activity(title, description, teamMembers, id)
