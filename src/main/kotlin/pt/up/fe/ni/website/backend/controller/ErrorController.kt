package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import jakarta.validation.ConstraintViolationException
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import pt.up.fe.ni.website.backend.config.Logging

data class SimpleError(
    val message: String,
    val param: String? = null,
    val value: Any? = null
)

data class CustomError(val errors: List<SimpleError>)

@RestController
@RestControllerAdvice
class ErrorController(private val objectMapper: ObjectMapper) : ErrorController, Logging {

    @RequestMapping("/**")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun endpointNotFound(): CustomError = wrapSimpleError("invalid endpoint")

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidArguments(e: ConstraintViolationException): CustomError {
        val errors = mutableListOf<SimpleError>()
        e.constraintViolations.forEach { violation ->
            errors.add(
                SimpleError(
                    violation.message,
                    violation.propertyPath.toString(),
                    violation.invalidValue.takeIf { it.isSerializable() }
                )
            )
        }
        return CustomError(errors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidRequestBody(e: HttpMessageNotReadableException): CustomError {
        when (val cause = e.cause) {
            is InvalidFormatException -> {
                val type = cause.targetType.simpleName.lowercase()
                return wrapSimpleError(
                    "must be $type",
                    value = cause.value
                )
            }

            is MissingKotlinParameterException -> {
                return wrapSimpleError(
                    "required",
                    param = cause.parameter.name
                )
            }

            is MismatchedInputException -> {
                return wrapSimpleError(
                    "must be ${cause.targetType.simpleName.lowercase()}",
                    param = cause.path.joinToString(".") { it.fieldName }
                )
            }
        }

        return wrapSimpleError(e.message ?: "invalid request body")
    }

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun elementNotFound(e: NoSuchElementException): CustomError {
        return wrapSimpleError(e.message ?: "element not found")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun illegalArgument(e: IllegalArgumentException): CustomError {
        return wrapSimpleError(e.message ?: "invalid argument")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun unexpectedError(e: Exception): CustomError {
        logger.error(e.message)
        return wrapSimpleError("unexpected error: " + e.message)
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun forbidden(e: AccessDeniedException): CustomError {
        return wrapSimpleError(e.message ?: "you don't have permission to access this resource")
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun invalidAuthentication(e: AuthenticationException): CustomError {
        return wrapSimpleError(e.message ?: "invalid authentication")
    }

    fun wrapSimpleError(msg: String, param: String? = null, value: Any? = null) = CustomError(
        mutableListOf(SimpleError(msg, param, value))
    )

    fun Any.isSerializable() = try {
        objectMapper.writeValueAsString(this)
        true
    } catch (err: Exception) {
        false
    }
}
