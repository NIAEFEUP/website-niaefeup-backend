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

    @Id @GeneratedValue
    val id: Long? = null
)
