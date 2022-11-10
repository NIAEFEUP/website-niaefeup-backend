package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants

@Entity
@EntityListeners(AuditingEntityListener::class)
class Post(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize,
        message = "size must be between ${Constants.Title.minSize} and ${Constants.Title.maxSize}")
    var title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Body.minSize, message = "size must be greater or equal to ${Constants.Body.minSize}")
    var body: String,

    @JsonProperty(required = true)
    @field:NotEmpty
    var thumbnailPath: String,

    @CreatedDate
    var publishDate: Date? = null,

    @LastModifiedDate
    var lastUpdatedAt: Date? = null,

    @Id @GeneratedValue
    val id: Long? = null
)
