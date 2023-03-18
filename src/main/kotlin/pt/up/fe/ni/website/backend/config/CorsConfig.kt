package pt.up.fe.ni.website.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
class CorsConfig {
    @field:Value("\${frontend.url}")
    final lateinit var url: String;

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.addAllowedOrigin(url)
        config.addAllowedHeader("*")
        config.addAllowedMethod("GET")
        config.addAllowedMethod("POST")
        config.addAllowedMethod("PUT")
        config.addAllowedMethod("PATCH")
        config.addAllowedMethod("DELETE")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
