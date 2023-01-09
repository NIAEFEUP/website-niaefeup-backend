package pt.up.fe.ni.website.backend.annotations.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateIntervalValidator::class])
@MustBeDocumented
annotation class DateInterval(
    val message: String = "{date_interval.error}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = [],
    val startDate: String,
    val endDate: String
)

class DateIntervalValidator : ConstraintValidator<DateInterval, Any> {
    private lateinit var startDateProp: String
    private lateinit var endDateProp: String

    override fun initialize(interval: DateInterval) {
        startDateProp = interval.startDate
        endDateProp = interval.endDate
    }

    override fun isValid(value: Any, context: ConstraintValidatorContext?): Boolean {
        val props = (value::class as KClass<in Any>).memberProperties
        val startDate = props.first { it.name == startDateProp }.getter(value) as Date?
        val endDate = props.first { it.name == endDateProp }.getter(value) as Date?

        return startDate == null || endDate == null || endDate.after(startDate)
    }
}
