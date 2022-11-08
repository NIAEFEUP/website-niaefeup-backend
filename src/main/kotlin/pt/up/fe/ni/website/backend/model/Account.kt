package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Past
import javax.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants

@Entity
class Account(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize)
    var name: String,

    @JsonProperty(required = true)
    @Column(unique = true)
    @field:NotEmpty
    @field:Email
    var email: String,

    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize)
    var bio: String?,

    @Temporal(TemporalType.DATE)
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

    @OneToMany // TODO doesn't seem to be working
    val websites: List<CustomWebsite>,

    @Id @GeneratedValue
    val id: Long? = null
)
