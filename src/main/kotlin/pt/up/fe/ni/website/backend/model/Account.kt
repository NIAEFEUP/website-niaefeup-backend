package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants

@Entity
class Account(
    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize)
    var name: String,

    @JsonProperty(required = true)
    @Column(unique = true)
    @field:NotEmpty
    @field:Email
    var email: String,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, required = true)
    @field:Size(min = Constants.Password.minSize, max = Constants.Password.maxSize)
    var password: String,

    @field:Size(min = Constants.Bio.minSize, max = Constants.Bio.maxSize)
    var bio: String?,

    @field:Past
    var birthDate: Date?,

    @field:NullOrNotBlank
    @field:URL
    var photoPath: String?,

    @field:NullOrNotBlank
    @field:URL
    var linkedin: String?,

    @field:NullOrNotBlank
    @field:URL
    var github: String?,

    @JoinColumn
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val websites: List<@Valid CustomWebsite> = emptyList(),

    @JoinColumn
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val roles: List<@Valid Role> = emptyList(),

    @Id @GeneratedValue
    val id: Long? = null
) {
    fun getEffectivePermissionsForActivity(activity: Activity): Permissions {
        val effectivePermissions = Permissions()

        roles.forEach { role ->
            effectivePermissions.addAll(role.permissions)

            role.perActivities
                .find { it.activity == activity }
                ?.let { effectivePermissions.addAll(it.permissions) }
        }

        return effectivePermissions
    }
}
