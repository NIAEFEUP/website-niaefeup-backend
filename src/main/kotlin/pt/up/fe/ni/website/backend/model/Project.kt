package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany

@Entity
class Project(
    title: String,
    description: String,

    @JoinColumn
    @OneToMany(fetch = FetchType.EAGER)
    var hallOfFame: MutableList<Account> = mutableListOf(),

    teamMembers: MutableList<Account> = mutableListOf(),
    associatedRoles: MutableList<PerActivityRole> = mutableListOf(),
    slug: String? = null,
    image: String,

    var isArchived: Boolean = false,
    val technologies: List<String> = emptyList(),
    var slogan: String? = null,
    var targetAudience: String,

    id: Long? = null
) : Activity(title, description, teamMembers, associatedRoles, slug, image, id)
