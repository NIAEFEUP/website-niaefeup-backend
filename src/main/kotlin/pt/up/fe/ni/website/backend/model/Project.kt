package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants

@Entity
class Project(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize)
    var name: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    var description: String,

    @JsonProperty(required = false)
    var isArchived: Boolean = false,

    @Id @GeneratedValue
    val id: Long? = null
)
