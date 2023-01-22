package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
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
    @Column(unique = true)
    var name: String,

    @JsonProperty(required = true)
    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @JsonProperty(required = true)
    var isSection: Boolean,

    @JoinColumn
    @ManyToMany
    var accounts: List<@Valid Account> = emptyList(),

    @JoinColumn
    @OneToMany
    var associatedActivities: List<@Valid PerActivityRole> = emptyList(),

    @JoinColumn
    @ManyToOne
    val generation: Generation,

    @JsonProperty(required = true)
    @Id
    @GeneratedValue
    val id: Long? = null
)
