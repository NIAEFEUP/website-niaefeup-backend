package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ConstraintViolationException

data class SimpleError(
    val message: String,
    val param: String? = null,
    val value: Any? = null
)

data class CustomError(val errors: List<SimpleError>)

@RestController
@RestControllerAdvice
class ErrorController : ErrorController {

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
                    violation.invalidValue
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
        }

        return wrapSimpleError(e.message ?: "invalid request body")
    }

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun elementNotFound(e: NoSuchElementException): CustomError {
        return wrapSimpleError(e.message ?: "element not found")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun unexpectedError(e: Exception): CustomError {
        System.err.println(e)
        return wrapSimpleError("unexpected error")
    }

    fun wrapSimpleError(msg: String, param: String? = null, value: Any? = null) = CustomError(
        mutableListOf(SimpleError(msg, param, value))
    )
}
