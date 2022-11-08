package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
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
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize)
    @field:Schema(
        description = "Title of the post",
        example = "Welcome new recruits",
        type = "string",
        minLength = Constants.Title.minSize,
        maxLength = Constants.Title.maxSize
    )
    var title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Body.minSize, message = "size must be greater or equal to ${Constants.Body.minSize}")
    @field:Schema(
        description = "Body of the post",
        example = "We want to congratulate you on joining our nucleus, but there's a lot of work ahead still. ...",
        type = "string",
        minLength = Constants.Body.minSize,
    )
    var body: String,

    @JsonProperty(required = true)
    @field:NotEmpty
    @field:Schema(
        description = "Path of the post image",
        type = "string",
    )
    var thumbnailPath: String,

    @CreatedDate
    @field:Schema(
        description = "Post creation date",
        example = "2021-05-12T14:00:00",
        type = "string"
    )
    var publishDate: Date? = null,

    @LastModifiedDate
    @field:Schema(
        description = "Post update date",
        example = "2022-05-12T14:00:00",
        type = "string"
    )
    var lastUpdatedAt: Date? = null,

    @Id @GeneratedValue
    @field:Schema(
        description = "ID of the post",
        example = "2",
        type = "integer",
        format = "int64"
    )
    val id: Long? = null
)
