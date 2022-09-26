package pt.up.fe.ni.website.backend.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import javax.validation.ConstraintViolationException
import javax.validation.Validation

open class Dto<T : Any> {

    companion object {
        private val objectMapper = ObjectMapper()
        private val validatorFactory = Validation.buildDefaultValidatorFactory()
    }

    fun update(entity: T): T {
        val newEntity = objectMapper.updateValue(entity, this)

        val validator = validatorFactory.validator
        val violations = validator.validate(newEntity)

        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }

        return newEntity
    }
}
