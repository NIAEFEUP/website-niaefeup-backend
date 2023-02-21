package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonBackReference
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
    @field:Convert(converter = PermissionsConverter::class)
    var permissions: Permissions,

    @Id @GeneratedValue
    val id: Long? = null,
) {
    @JoinColumn
    @ManyToOne // TODO: Perhaps change to sending only ID
    lateinit var activity: Activity

    @JoinColumn
    @ManyToOne
    @JsonBackReference
    lateinit var role: Role
}
