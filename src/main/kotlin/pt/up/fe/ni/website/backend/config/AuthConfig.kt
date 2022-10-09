package pt.up.fe.ni.website.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class AuthConfig(val rsaKeys: RSAKeyProperties) {
    @Bean
    fun user(): InMemoryUserDetailsManager {
        return InMemoryUserDetailsManager(
            User.withUsername("admin").password("{noop}admin123").authorities("read").build()
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .authorizeRequests { auth -> auth.anyRequest().permitAll() }
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer<HttpSecurity>::jwt)
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic(Customizer.withDefaults())
            .build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(rsaKeys::publicKey.get()).build()
    }
}
