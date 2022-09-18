package pt.up.fe.ni.website.backend.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
@EntityListeners(AuditingEntityListener::class)
class Post(
    var title: String,
    var body: String,
    var thumbnailPath: String,
    @CreatedDate var publishDate: Date? = null,
    @LastModifiedDate var lastUpdatedAt: Date? = null,
    @Id @GeneratedValue val id: Long? = null
)
