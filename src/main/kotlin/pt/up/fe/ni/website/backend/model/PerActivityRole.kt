package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.model.permissions.PermissionsConverter

@Entity
class PerActivityRole(
    @JoinColumn
    @ManyToOne
    var role: Role,

    @JoinColumn
    @ManyToOne
    var activity: Activity,

    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @Id @GeneratedValue
    var id: Long? = null
)
