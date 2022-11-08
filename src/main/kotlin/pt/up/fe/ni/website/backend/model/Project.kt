package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.ProjectConstants as Constants

@Entity
class Project(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Name.minSize, max = Constants.Name.maxSize)
    @field:Schema(
        description = "Name of the project",
        example = "Uni",
        type = "string",
        minLength = Constants.Name.minSize,
        maxLength = Constants.Name.maxSize
    )
    var name: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    @field:Schema(
        description = "Description of the project",
        example = "Uni bring the information of the various UPorto services to the tip of students' fingers.",
        type = "string",
        minLength = Constants.Description.minSize,
        maxLength = Constants.Description.maxSize
    )
    var description: String,

    @Id @GeneratedValue
    @field:Schema(
        description = "ID of the project",
        example = "2",
        type = "integer",
        format = "int64"
    )
    val id: Long? = null
)
