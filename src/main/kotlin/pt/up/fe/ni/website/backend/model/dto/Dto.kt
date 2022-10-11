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

        /*
         * @Suppress("UNCHECKED_CAST") is a hint to the compiler to suppress all warnings which are related to unchecked
         * casts.
         *
         * When you cast an object to a string, the JVM checks if that object really is a String before proceeding.
         * If it's not, it'll throw a cast exception. That's the reason why you should always use instanceof before any cast.
         *
         * But what about List? What happens if I want to convert a List<Integer> to a List<String>? You can't really
         * cast a String to an Integer, but generics don't really exist at runtime, so, in practice, you'd be converting
         * from List to List, which seems valid to the JVM. It doesn't throw a runtime error. That's an unchecked cast.
         *
         * Here, we are converting from KClass<*> to KClass<T>, which follows the same logic.
         *
         * ```
         * val strings: List<String> = listOf("one", "two", "three")
         * println(strings.joinToString(", "))
         *
         * val numbers: List<Int> = strings as List<Int>
         * println(numbers.joinToString(", "))
         * ```
         *
         * This code runs without errors and the output is
         * ```
         * one, two, three
         * one, two, three
         * ```
         */
        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> getTypeConversionClass(clazz: KClass<out Dto<T>>): KClass<T>? {
            val thisType = clazz.supertypes.first { it.classifier == Dto::class }

            /*
             * I don't really know *how* jvmErasure works...
             * It's a Kotlin Reflect feature that seems to completely recover generic type information at runtime.
             * We can use it to determine the class of T, the type argument of Dto.
             */
            return thisType.arguments.firstOrNull()?.type?.jvmErasure as KClass<T>?
        }

        /*
         * See ValidationUtils#getTypeConversionClass for information about @Suppress("UNCHECKED_CAST")
         *
         * There is a performance hit when using reflection. Reflective operations take a lot of time since they can't
         * be optimized by the Java compiler at compile-time. As a rule of thumb, you should minimize the amount of
         * reflective operations in your code, and since the type argument stays the same for every different kind of
         * Dto, we can cache it, thus reducing the amount of reflective operations performed.
         *
         * The goal behind `genericArgumentClass` is to reuse code. We have a type cache and all types in the cache 
         * are verified to be types that are annotated with @Entity. With `genericArgumentClass`, we skip the
         * automatic type determination but we still ensure it's an entity before putting it on the cache.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getTypeConversionClassWithCache(clazz: KClass<out Dto<T>>, genericArgumentClass: KClass<T>? = null): KClass<T> {
            if (clazz == Dto::class) throw IllegalCallerException("DTO is not extended by any class")
            if (!typeArgumentCache.containsKey(clazz)) {
                val typeArgumentErasure = genericArgumentClass ?: getTypeConversionClass(clazz)
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
