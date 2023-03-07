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
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

abstract class TemplateEmailBuilder<T>(
    private val template: String
) : BaseEmailBuilder() {
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

        val commonmarkParser = Parser.builder().extensions(
            listOf(
                YamlFrontMatterExtension.create()
            )
        ).build()
        val commonmarkHtmlRenderer = HtmlRenderer.builder().build()
        val commonmarkTextRenderer = TextContentRenderer.builder().build()

        val doc = commonmarkParser.parse(markdown)

        val html = commonmarkHtmlRenderer.render(doc)
        val text = commonmarkTextRenderer.render(doc)

        helper.setText(text, html)

        val yamlVisitor = YamlFrontMatterVisitor()
        doc.accept(yamlVisitor)

        val subject = yamlVisitor.data["subject"]?.getOrNull(0)
        if (subject != null) {
            helper.setSubject(subject)
        }

        print(yamlVisitor.data)

        yamlVisitor.data.getOrDefault("attachments", emptyList()).forEach { addFile(helper::addAttachment, it) }
        yamlVisitor.data.getOrDefault("inline", emptyList()).forEach { addFile(helper::addInline, it) }
    }

    private fun addFile(fn: (String, Resource) -> Any, file: String) {
        val split = file.split("\\s*::\\s*".toRegex())

        if (split.size != 2) {
            return
        }

        val name = split[0]
        val path = split[1]

        fn(name, UrlResource(path))
    }
}
