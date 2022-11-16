package pt.up.fe.ni.website.backend.annotations.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NullOrNotBlankValidator::class])
@MustBeDocumented
annotation class NullOrNotBlank(
    val message: String = "must be null or not blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

class NullOrNotBlankValidator : ConstraintValidator<NullOrNotBlank, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value == null || value.isNotBlank()
    }
}
