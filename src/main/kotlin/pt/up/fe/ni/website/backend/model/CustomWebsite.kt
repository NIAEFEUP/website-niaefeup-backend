package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.utils.validation.NullOrNotBlank

@Entity
class CustomWebsite(
    @JsonProperty(required = true)
    @field:NotEmpty
    @field:URL
    val url: String,

    @field:NullOrNotBlank
    @field:URL
    val iconPath: String?,

    @Id @GeneratedValue
    val id: Long? = null
)
