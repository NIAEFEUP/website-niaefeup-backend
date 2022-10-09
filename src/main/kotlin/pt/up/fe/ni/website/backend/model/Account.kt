package pt.up.fe.ni.website.backend.model

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class Account(
    @Column(nullable = false)
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    val photo: String?,
    val linkedin: String?,
    @OneToMany
    val websites: List<CustomWebsite>,

    val permissions: Long,

    @ManyToOne
    val role: Role,

    @Id @GeneratedValue
    val id: Long? = null
)
