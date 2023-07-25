package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Entity
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import pt.up.fe.ni.website.backend.config.ApplicationContextUtils

abstract class EntityDto<T : Any> {

    @JsonIgnore
    private val entityClass: KClass<T>

    constructor() : this(null)

    /**
     * Takes the entity class, used to avoid reflection. This class is still cached and verified to be a valid entity.
     * @param conversionClass The entity class to convert to. If null, the class will be determined using reflection.
     */
    constructor(conversionClass: KClass<T>?) {
        this.entityClass = DtoReflectionUtils.getTypeConversionClassWithCache(this::class, conversionClass)
    }

    companion object {
        private val objectMapper = ApplicationContextUtils.getBean(ObjectMapper::class.java)
        private val validator = ApplicationContextUtils.getBean(Validator::class.java)
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
        val violations = validator.validate(entity)

        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }

        return entity
    }

    object DtoReflectionUtils {
        /** Reduce reflection operations by caching the type class for each EntityDto */
        private val typeArgumentCache = HashMap<KClass<out EntityDto<*>>, KClass<*>>()

        // The use of suppress is explained at https://github.com/NIAEFEUP/website-niaefeup-backend/pull/20#discussion_r985236224
        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> getTypeConversionClass(clazz: KClass<out EntityDto<T>>): KClass<T>? {
            val superType = clazz.supertypes.firstOrNull { it.jvmErasure.isSubclassOf(EntityDto::class) } ?: return null
            val conversionClassArg =
                superType.arguments.firstOrNull {
                    it.type?.jvmErasure?.findAnnotation<Entity>() != null
                } ?: return getTypeConversionClass(superType.jvmErasure as KClass<out EntityDto<T>>)

            return conversionClassArg.type?.jvmErasure as KClass<T>?
        }

        /**
         * Gets the entity class for the given EntityDto class, using cache if possible.
         * @param clazz The EntityDto class to get the entity class for (usually this::class).
         * @param conversionClass Optional entity class, if one wants to avoid reflection.
         * If null, the class will be determined automatically and cached.
         */
        // The use of suppress is explained at https://github.com/NIAEFEUP/website-niaefeup-backend/pull/20#discussion_r985236224
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getTypeConversionClassWithCache(
            clazz: KClass<out EntityDto<T>>,
            conversionClass: KClass<T>? = null
        ): KClass<T> {
            if (clazz == EntityDto::class) throw IllegalCallerException("DTO is not extended by any class")
            if (!typeArgumentCache.containsKey(clazz)) {
                val typeArgumentErasure = conversionClass ?: getTypeConversionClass(clazz)
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
