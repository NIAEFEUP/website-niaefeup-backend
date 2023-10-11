package pt.up.fe.ni.website.backend.email

import com.samskivert.mustache.Mustache
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.text.TextContentRenderer
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.util.ResourceUtils
import pt.up.fe.ni.website.backend.config.ApplicationContextUtils

abstract class TemplateEmailBuilder<T>(
    private val template: String
) : BaseEmailBuilder() {
    private val commonmarkParser = ApplicationContextUtils.getBean(Parser::class.java)
    private val commonmarkHtmlRenderer = ApplicationContextUtils.getBean(HtmlRenderer::class.java)
    private val commonmarkTextRenderer = ApplicationContextUtils.getBean(TextContentRenderer::class.java)
    private val mustache = ApplicationContextUtils.getBean(Mustache.Compiler::class.java)

    private var data: T? = null

    fun data(data: T) = apply {
        this.data = data
    }

    override fun build(helper: MimeMessageHelper) {
        super.build(helper)

        if (data == null) return

        val markdown = mustache.loadTemplate(template).execute(data)

        val doc = commonmarkParser.parse(markdown)
        val htmlContent = commonmarkHtmlRenderer.render(doc)
        val text = commonmarkTextRenderer.render(doc)

        val yamlVisitor = YamlFrontMatterVisitor()
        doc.accept(yamlVisitor)

        val subject = yamlVisitor.data["subject"]?.firstOrNull()
        subject?.let { helper.setSubject(it) }

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

        yamlVisitor.data.getOrDefault("attachments", emptyList()).forEach { addFile(helper::addAttachment, it) }
        yamlVisitor.data.getOrDefault("inline", emptyList()).forEach { addFile(helper::addInline, it) }
    }

    private fun addFile(fn: (String, Resource) -> Any, file: String) {
        val split = file.split("\\s*::\\s*".toRegex(), 2)

        if (split.isEmpty()) return

        val name = split[0]
        val path = split.getOrElse(1) { split[0] }

        fn(name, UrlResource(path))
    }
}
