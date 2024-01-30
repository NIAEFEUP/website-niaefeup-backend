package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Activity(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize)
    open var title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    open var description: String,

    @JoinColumn
    @OneToMany(fetch = FetchType.EAGER)
    open val teamMembers: MutableList<Account>,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "activity")
    @JsonIgnore // TODO: Decide if we want to return perRoles (or IDs) by default
    open val associatedRoles: MutableList<@Valid PerActivityRole> = mutableListOf(),

    @Column(unique = true)
    @field:Size(min = Constants.Slug.minSize, max = Constants.Slug.maxSize)
    open val slug: String? = null,

    @field:NotBlank
    open var image: String,

    val gallery: MutableList<String> = mutableListOf(),

    @Id
    @GeneratedValue
    open val id: Long? = null
)
