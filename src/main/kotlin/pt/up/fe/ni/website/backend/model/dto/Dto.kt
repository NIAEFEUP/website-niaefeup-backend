package pt.up.fe.ni.website.backend.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.Entity
import javax.validation.ConstraintViolationException
import javax.validation.Validation
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

open class Dto<T : Any> {

    @JsonIgnore
    private val entityClass: KClass<T>

    constructor() : this(null)

    constructor(conversionClass: KClass<T>?) {
        this.entityClass = DtoReflectionUtils.getTypeConversionClassWithCache(this::class, conversionClass)
    }

    companion object {
        private val objectMapper = ObjectMapper()
        private val validatorFactory = Validation.buildDefaultValidatorFactory()
    }

    fun create(): T {
        val newEntity = objectMapper.convertValue(this, entityClass.java)
        return ensureValid(newEntity)
    }

    fun update(entity: T): T {
        val newEntity = objectMapper.updateValue(entity, this)
        return ensureValid(newEntity)
    }

    private fun ensureValid(entity: T): T {
        val validator = validatorFactory.validator
        val violations = validator.validate(entity)

        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }

        return entity
    }

    object DtoReflectionUtils {
        private val typeArgumentCache = HashMap<KClass<out Dto<*>>, KClass<*>>()

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> getTypeConversionClass(clazz: KClass<out Dto<T>>): KClass<T>? {
            val thisType = clazz.supertypes.first { it.classifier == Dto::class }
            return thisType.arguments.firstOrNull()?.type?.jvmErasure as KClass<T>?
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getTypeConversionClassWithCache(clazz: KClass<out Dto<T>>, fallback: KClass<T>? = null): KClass<T> {
            if (clazz == Dto::class) throw IllegalCallerException("DTO is not extended by any class")
            if (!typeArgumentCache.containsKey(clazz)) {
                val typeArgumentErasure = fallback ?: getTypeConversionClass(clazz)
                typeArgumentCache[clazz] = ensureEntity(typeArgumentErasure)
            }

            return typeArgumentCache[clazz] as KClass<T>
        }

        private fun <T : Any> ensureEntity(clazz: KClass<T>?): KClass<T> {
            if (clazz == null || !clazz.hasAnnotation<Entity>()) {
                throw IllegalArgumentException("$clazz is not a DTO of an entity")
            }

            return clazz
        }
    }
}
