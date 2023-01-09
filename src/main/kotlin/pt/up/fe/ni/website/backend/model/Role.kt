package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import pt.up.fe.ni.website.backend.permissions.Permissions
import pt.up.fe.ni.website.backend.permissions.PermissionsConverter

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

    @ManyToMany
    var accounts: List<Account>,

    @OneToMany
    var perActivities: List<PerActivityRole>,

    @JsonProperty(required = true)
    @Id @GeneratedValue
    val id: Long? = null
)
