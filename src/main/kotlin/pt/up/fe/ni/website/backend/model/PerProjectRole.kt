package pt.up.fe.ni.website.backend.model

import pt.up.fe.ni.website.backend.permissions.Permissions
import pt.up.fe.ni.website.backend.permissions.PermissionsConverter
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class PerProjectRole(
    @ManyToOne
    var role: Role,

    @ManyToOne
    var project: Project,

    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @Id @GeneratedValue
    var id: Long? = null
)
