package pt.up.fe.ni.website.backend.config.email

import com.samskivert.mustache.Mustache
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.text.TextContentRenderer
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmailConfig(
    private val emailConfigProperties: EmailConfigProperties
) {
    @Bean
    fun mustacheCompiler() = Mustache.compiler().withLoader(
        MustacheResourceTemplateLoader(emailConfigProperties.templatePrefix, emailConfigProperties.templateSuffix)
    )

    @Bean
    fun commonmarkParser() = Parser.builder().extensions(
        listOf(
            YamlFrontMatterExtension.create()
        )
    ).build()

    @Bean
    fun commonmarkHtmlRenderer() = HtmlRenderer.builder().build()

    @Bean
    fun commonmarkTextRenderer() = TextContentRenderer.builder().build()
}
