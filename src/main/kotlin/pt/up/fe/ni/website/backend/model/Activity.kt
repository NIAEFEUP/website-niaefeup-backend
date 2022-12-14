package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Activity(

    @JsonProperty(required = true)
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize)
    open val title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Description.minSize, max = Constants.Description.maxSize)
    open val description: String,

    @Id
    @GeneratedValue
    open val id: Long? = null
)
