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

@Entity
@EntityListeners(AuditingEntityListener::class)
class Post(
    @JsonProperty(required = true)
    @field:Size(min = 2, max = 500)
    var title: String,

    @JsonProperty(required = true)
    @field:Size(min = 10, message = "size must be greater than 10")
    var body: String,

    @field:NotEmpty
    var thumbnailPath: String,

    @CreatedDate
    var publishDate: Date? = null,

    @LastModifiedDate
    var lastUpdatedAt: Date? = null,

    @Id @GeneratedValue
    val id: Long? = null
) {
    data class PatchModel(
        @field:Size(min = 2, max = 500)
        var title: String?,

        @field:Size(min = 10, message = "size must be greater than 10")
        var body: String?,

        // Using @Size because @NotEmpty doesn't validate nulls
        @field:Size(min = 1, message = "must not be empty")
        var thumbnailPath: String?
    )
}
