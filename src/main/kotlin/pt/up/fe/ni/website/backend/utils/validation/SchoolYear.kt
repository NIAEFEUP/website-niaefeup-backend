package pt.up.fe.ni.website.backend.utils.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SchoolYearValidator::class])
@MustBeDocumented
annotation class SchoolYear(
    val message: String = "{school_year.error}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

class SchoolYearValidator : ConstraintValidator<SchoolYear, String> {
    private val regex = Regex("\\d{2}-\\d{2}")

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        if (!value.matches(regex)) return false

        val years = value.split("-")
        if (years.size != 2) return false

        val firstYear = years[0].toIntOrNull() ?: return false
        val secondYear = years[1].toIntOrNull() ?: return false
        return secondYear == firstYear + 1
    }
}
