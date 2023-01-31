package pt.up.fe.ni.website.backend.annotations.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import pt.up.fe.ni.website.backend.model.Role
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoDuplicateRolesValidator::class])
@MustBeDocumented
annotation class NoDuplicateRoles(
    val message: String = "{no_duplicate_roles.error}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

class NoDuplicateRolesValidator : ConstraintValidator<NoDuplicateRoles, List<Role>> {
    override fun isValid(value: List<Role>, context: ConstraintValidatorContext?): Boolean {
        val names = value.map { it.name }
        return names.size == names.toSet().size
    }
}
