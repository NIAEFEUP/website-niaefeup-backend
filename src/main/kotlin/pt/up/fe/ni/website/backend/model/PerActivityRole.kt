package pt.up.fe.ni.website.backend.model

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import pt.up.fe.ni.website.backend.permissions.Permissions
import pt.up.fe.ni.website.backend.permissions.PermissionsConverter

@Entity
class PerActivityRole(
    @ManyToOne
    var role: Role,

    @ManyToOne
    var activity: Activity,

    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @Id @GeneratedValue
    var id: Long? = null
)
