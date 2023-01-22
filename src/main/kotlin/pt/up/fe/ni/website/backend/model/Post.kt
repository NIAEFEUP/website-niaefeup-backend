package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants

@Entity
@EntityListeners(AuditingEntityListener::class)
class Post(
    @JsonProperty(required = true)
    @field:Size(min = Constants.Title.minSize, max = Constants.Title.maxSize)
    var title: String,

    @JsonProperty(required = true)
    @field:Size(min = Constants.Body.minSize, message = "{size.min}")
    var body: String,

    @JsonProperty(required = true)
    @field:NotEmpty
    @field:URL
    var thumbnailPath: String,

    @CreatedDate
    var publishDate: Date? = null,

    @LastModifiedDate
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    var lastUpdatedAt: Date? = null,

    @Id @GeneratedValue
    val id: Long? = null,

    @Column(unique = true)
    @field:Size(min = Constants.Slug.minSize, max = Constants.Slug.maxSize)
    val slug: String? = null
)
