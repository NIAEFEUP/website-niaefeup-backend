package pt.up.fe.ni.website.backend.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderColumn
import jakarta.validation.Valid
import pt.up.fe.ni.website.backend.annotations.validation.NoDuplicateRoles
import pt.up.fe.ni.website.backend.annotations.validation.SchoolYear

@Entity
class Generation(
    @JsonProperty(required = true)
    @Column(unique = true)
    @field:SchoolYear
    var schoolYear: String,

    @Id @GeneratedValue
    val id: Long? = null
) {
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "generation")
    @OrderColumn
    @JsonManagedReference
    @field:NoDuplicateRoles
    val roles: MutableList<@Valid Role> = mutableListOf()
}
