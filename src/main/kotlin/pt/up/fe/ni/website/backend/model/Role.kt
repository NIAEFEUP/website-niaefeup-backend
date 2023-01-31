package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.validation.Valid
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.model.permissions.PermissionsConverter

@Entity
class Role(
    @JsonProperty(required = true)
    var name: String,

    @JsonProperty(required = true)
    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @JsonProperty(required = true)
    var isSection: Boolean,

    @JoinColumn
    @ManyToMany
    @JsonIgnore // TODO: Decide if we want to return accounts (or IDs) by default
    val accounts: MutableList<@Valid Account> = mutableListOf(),

    @JsonProperty(required = true)
    @Id
    @GeneratedValue
    val id: Long? = null
) {
    @JoinColumn
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JsonManagedReference
    var associatedActivities: MutableList<@Valid PerActivityRole> = mutableListOf()

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    lateinit var generation: Generation
}
