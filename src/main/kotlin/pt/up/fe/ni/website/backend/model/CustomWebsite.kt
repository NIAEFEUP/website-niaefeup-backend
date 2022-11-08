package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.URL
import pt.up.fe.ni.website.backend.annotations.validation.NullOrNotBlank
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class CustomWebsite(
    @JsonProperty(required = true)
    @field:URL
    val url: String,

    @field:NullOrNotBlank
    @field:URL
    val iconPath: String?,

    @Id @GeneratedValue
    val id: Long? = null
)
