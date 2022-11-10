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
    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize,
        message = "size must be between ${Constants.Name.minSize} and ${Constants.Name.maxSize}")
    var name: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize,
        message = "size must be between ${Constants.Description.minSize} and ${Constants.Description.maxSize}")
    var description: String,

    @Id @GeneratedValue
    val id: Long? = null
)
