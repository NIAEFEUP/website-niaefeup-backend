package pt.up.fe.ni.website.backend.model

import javax.persistence.Entity

@Entity
class Project(
    title: String,
    description: String,
    teamMembers: MutableList<Account>,
    var isArchived: Boolean = false,
    id: Long? = null
) : Activity(title, description, teamMembers, id)
