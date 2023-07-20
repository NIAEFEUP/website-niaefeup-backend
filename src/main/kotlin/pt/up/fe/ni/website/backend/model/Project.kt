package pt.up.fe.ni.website.backend.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlank

@Entity
class Project(
    title: String,
    description: String,
    teamMembers: MutableList<Account> = mutableListOf(),
    associatedRoles: MutableList<PerActivityRole> = mutableListOf(),
    slug: String? = null,
    image: String,

    var isArchived: Boolean = false,

    val technologies: List<String> = emptyList(),

    @field:NullOrNotBlank
    var slogan: String? = null,

    @field:NotEmpty
    var targetAudience: String,

    @JoinColumn
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val links: List<@Valid CustomWebsite> = emptyList(),

    @JoinColumn
    @OneToMany(fetch = FetchType.EAGER)
    var hallOfFame: MutableList<Account> = mutableListOf(),

    id: Long? = null
) : Activity(title, description, teamMembers, associatedRoles, slug, image, id)
