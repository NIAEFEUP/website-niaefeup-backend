package pt.up.fe.ni.website.backend.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlank

@Entity
class Project(
    title: String,
    description: String,
    teamMembers: MutableList<Account> = mutableListOf(),
    associatedRoles: MutableList<PerActivityRole> = mutableListOf(),
    slug: String? = null,
    image: String,
    gallery: MutableList<String> = mutableListOf(),

    var isArchived: Boolean = false,

    val technologies: List<String> = emptyList(),

    @field:Size(min = Constants.Slogan.minSize, max = Constants.Slogan.maxSize)
    var slogan: String? = null,

    @field:Size(min = Constants.TargetAudience.minSize, max = Constants.TargetAudience.maxSize)
    var targetAudience: String,

    @field:NullOrNotBlank
    @field:URL
    var github: String? = null,

    @JoinColumn
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val links: List<@Valid CustomWebsite> = emptyList(),

    @JoinColumn
    @OneToMany(fetch = FetchType.EAGER)
    var hallOfFame: MutableList<Account> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @OrderBy("date")
    val timeline: List<@Valid TimelineEvent> = emptyList(),

    id: Long? = null
) : Activity(title, description, teamMembers, associatedRoles, slug, image, gallery, id)
