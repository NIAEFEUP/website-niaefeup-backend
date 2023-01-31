package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embedded
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval

@Entity
class Event(
    title: String,
    description: String,
    teamMembers: MutableList<Account> = mutableListOf(),

    @field:NullOrNotBlank
    @field:URL
    var registerUrl: String? = null,

    @Embedded
    @field:Valid
    val dateInterval: DateInterval,

    @field:Size(min = Constants.Location.minSize, max = Constants.Location.maxSize)
    val location: String?,

    @field:Size(min = Constants.Category.minSize, max = Constants.Category.maxSize)
    val category: String?,

    @JsonProperty(required = true)
    @field:NotEmpty
    @field:URL
    val thumbnailPath: String,

    associatedRoles: List<PerActivityRole> = emptyList(),
    id: Long? = null,

    @Column(unique = true)
    val slug: String? = null

) : Activity(title, description, teamMembers, associatedRoles, id)
