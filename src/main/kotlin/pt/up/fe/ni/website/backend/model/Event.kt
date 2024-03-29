package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlank

@Entity
class Event(
    title: String,
    description: String,
    teamMembers: MutableList<Account> = mutableListOf(),
    associatedRoles: MutableList<PerActivityRole> = mutableListOf(),
    slug: String? = null,
    image: String,

    @field:NullOrNotBlank
    @field:URL
    var registerUrl: String? = null,

    @Embedded
    @field:Valid
    var dateInterval: DateInterval,

    @field:Size(min = Constants.Location.minSize, max = Constants.Location.maxSize)
    val location: String?,

    @field:Size(min = Constants.Category.minSize, max = Constants.Category.maxSize)
    val category: String?,

    id: Long? = null
) : Activity(title, description, teamMembers, associatedRoles, slug, image, id)
