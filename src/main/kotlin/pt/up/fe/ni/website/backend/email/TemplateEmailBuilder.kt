package pt.up.fe.ni.website.backend.email

import com.samskivert.mustache.Mustache
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.text.TextContentRenderer
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.util.ResourceUtils
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

abstract class TemplateEmailBuilder<T>(
    private val template: String
) : BaseEmailBuilder() {
    private companion object {
        val commonmarkParser: Parser = Parser.builder().extensions(
            listOf(
                YamlFrontMatterExtension.create()
            )
        ).build()
        val commonmarkHtmlRenderer: HtmlRenderer = HtmlRenderer.builder().build()
        val commonmarkTextRenderer: TextContentRenderer = TextContentRenderer.builder().build()
    }

    private var data: T? = null

    fun data(data: T) = apply {
        this.data = data
    }

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        super.build(helper, emailConfigProperties)

        val mustache = Mustache.compiler().withLoader(
            MustacheResourceTemplateLoader(emailConfigProperties.templatePrefix, emailConfigProperties.templateSuffix)
        )

        val markdown = mustache.loadTemplate(template).execute(data)

        val doc = commonmarkParser.parse(markdown)
        val htmlContent = commonmarkHtmlRenderer.render(doc)
        val text = commonmarkTextRenderer.render(doc)

        val yamlVisitor = YamlFrontMatterVisitor()
        doc.accept(yamlVisitor)

        val subject = yamlVisitor.data["subject"]?.firstOrNull()
        if (subject != null) {
            helper.setSubject(subject)
        }

        yamlVisitor.data.getOrDefault("attachments", emptyList()).forEach { addFile(helper::addAttachment, it) }
        yamlVisitor.data.getOrDefault("inline", emptyList()).forEach { addFile(helper::addInline, it) }

        val styles = yamlVisitor.data.getOrDefault("styles", mutableListOf()).apply {
            if (yamlVisitor.data["no_default_style"].isNullOrEmpty()) {
                this.add(emailConfigProperties.defaultStyle)
            }
        }.map {
            ResourceUtils.getFile(it).readText()
        }

        val htmlTemplate = yamlVisitor.data["layout"]?.firstOrNull() ?: emailConfigProperties.defaultHtmlLayout
        val html = mustache.loadTemplate(htmlTemplate).execute(
            mapOf(
                "subject" to subject,
                "content" to htmlContent,
                "styles" to styles
            )
        )

        helper.setText(text, html)
    }

    private fun addFile(fn: (String, Resource) -> Any, file: String): Pair<String, String>? {
        var split = file.split("\\s*::\\s*".toRegex(), 2)

        if (split.isEmpty()) {
            return null
        } else if (split.size == 1) {
            split = listOf(split[0], split[0])
        }

        val name = split[0]
        val path = split[1]

        fn(name, UrlResource(path))

        return Pair(name, path)
    }
}
