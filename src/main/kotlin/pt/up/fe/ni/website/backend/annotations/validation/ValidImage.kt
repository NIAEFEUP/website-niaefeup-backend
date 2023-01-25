package pt.up.fe.ni.website.backend.annotations.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.web.multipart.MultipartFile
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidImageValidator::class])
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidImage(
    val message: String = "{files.invalid_image}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = [],
)

class ValidImageValidator : ConstraintValidator<ValidImage, MultipartFile?> {

    override fun isValid(value: MultipartFile?, context: ConstraintValidatorContext?): Boolean {

        if (value == null) {
            return true
        }

        if (!isSupportedContentType(value.contentType)) {
            return false
        }

        return true
    }

    private fun isSupportedContentType(contentType: String?): Boolean {
        return contentType == "image/png" || contentType == "image/jpg" || contentType == "image/jpeg"
    }
}
