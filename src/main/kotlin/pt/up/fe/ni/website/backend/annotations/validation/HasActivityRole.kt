package pt.up.fe.ni.website.backend.annotations.validation

import org.springframework.core.annotation.AliasFor
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("")
annotation class HasActivityRole(
    @get:AliasFor(annotation = PreAuthorize::class, attribute = "value")
    val perActivityRole: String
)
