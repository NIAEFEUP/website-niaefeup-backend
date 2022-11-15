package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import pt.up.fe.ni.website.backend.permissions.Permissions
import pt.up.fe.ni.website.backend.permissions.PermissionsConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany

@Entity
class Role(
    @JsonProperty(required = true)
    @Column(unique = true)
    val name: String,

    @JsonProperty(required = true)
    @field:Convert(converter = PermissionsConverter::class)
    val permissions: Permissions,

    @JsonProperty(required = true)
    val isSection: Boolean,

    @ManyToMany
    val accounts: List<Account>,

    @JsonProperty(required = true)
    @Id @GeneratedValue
    val id: Long? = null
)
