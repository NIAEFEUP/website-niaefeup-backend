package pt.up.fe.ni.website.backend.model

import java.net.URL
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
class Account (
    @Column(nullable = false)
    val name: String,
    val bio: String?,
    val birthDate: Date?,
    val photo: String?,
    val linkedin: String?,
    @OneToMany
    val websites: List<CustomWebsite>,

    @ManyToOne
    val role: Role,

    @Id @GeneratedValue
    val id: Long? = null,
)
