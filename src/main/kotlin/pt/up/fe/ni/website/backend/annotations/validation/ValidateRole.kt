package pt.up.fe.ni.website.backend.annotations.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@PreAuthorize("hasRole(#)") //TODO: Find way to get the value of the role parameter
annotation class ValidateRole(
    val role: String,
    val message: String = "{validate_role.error}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidateRoleValidator : ConstraintValidator<ValidateRole, String> {
    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        // Validation is performed by the @PreAuthorize annotation
        return true
    }
}
