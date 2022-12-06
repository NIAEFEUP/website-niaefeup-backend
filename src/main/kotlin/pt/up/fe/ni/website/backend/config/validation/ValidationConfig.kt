package pt.up.fe.ni.website.backend.config.validation

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.Validator

@Configuration
class ValidationConfig {
    @Bean
    fun validatorFactory(messageSource: MessageSource): Validator {
        val validator = LocalValidatorFactoryBean()
        validator.setValidationMessageSource(messageSource)
        return validator
    }

    @Bean
    fun messageSource(): MessageSource {
        val bean = ReloadableResourceBundleMessageSource()
        bean.addBasenames(
            "classpath:validation_errors"
        )
        bean.setDefaultEncoding("UTF-8")
        return bean
    }
}
