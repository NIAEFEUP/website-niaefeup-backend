package pt.up.fe.ni.website.backend.utils.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateIntervalValidator::class])
@MustBeDocumented
annotation class ValidDateInterval(
    val message: String = "{date_interval.error}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

class DateIntervalValidator : ConstraintValidator<ValidDateInterval, DateInterval> {
    override fun isValid(value: DateInterval, context: ConstraintValidatorContext?): Boolean {
        return value.endDate == null || value.endDate.after(value.startDate)
    }
}
