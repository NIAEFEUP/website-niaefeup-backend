package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Project(
    title: String,
    description: String,
    teamMembers: MutableList<Account> = mutableListOf(),
    var isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    associatedRoles: List<PerActivityRole> = emptyList(),
    id: Long? = null,

    @Column(unique = true)
    val slug: String? = null
) : Activity(title, description, teamMembers, associatedRoles, id)
