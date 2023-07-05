package pt.up.fe.ni.website.backend.config.auth

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.HandlerExceptionResolver

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class AuthConfig(
    val authConfigProperties: AuthConfigProperties,
    @Qualifier("handlerExceptionResolver") val exceptionResolver: HandlerExceptionResolver
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.headers().frameOptions().disable()
        return http.csrf { csrf -> csrf.disable() }.cors().and()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(rolesConverter())
            .and().authenticationEntryPoint { request, response, exception ->
                exceptionResolver.resolveException(request, response, null, exception)
            }.and()
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic().disable().build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(authConfigProperties::publicKey.get()).build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val jwt = RSAKey.Builder(authConfigProperties::publicKey.get()).privateKey(
            authConfigProperties::privateKey.get()
        ).build()
        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwt)))
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    fun rolesConverter(): JwtAuthenticationConverter? {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        authoritiesConverter.setAuthorityPrefix("ROLE_")
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        return converter
    }
}
