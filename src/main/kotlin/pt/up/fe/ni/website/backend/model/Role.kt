package pt.up.fe.ni.website.backend.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Role(
    val name: String,
    val permissions: Long, // 64 permissions
    val boardMember: Boolean,
    val boardPosition: Short?,

    @Id @GeneratedValue
    val id: Long? = null,
)
