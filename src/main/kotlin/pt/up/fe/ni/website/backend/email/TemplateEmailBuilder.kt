package pt.up.fe.ni.website.backend.email

import com.samskivert.mustache.Mustache
import java.io.ByteArrayInputStream
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

abstract class TemplateEmailBuilder<T> : BaseEmailBuilder() {
    private var data: T? = null

    protected open fun subject(data: T?): String? = null
    protected open fun htmlTemplatePath(data: T?): String? = null
    protected open fun textTemplatePath(data: T?): String? = null
    protected open fun attachments(data: T?): List<TemplateAttachment> = emptyList()

    fun data(data: T) = apply {
        this.data = data
    }

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        super.build(helper, emailConfigProperties)

        val subject = subject(data)
        if (subject != null) {
            helper.setSubject(subject)
        }

        val mustache = Mustache.compiler().withLoader(
            MustacheResourceTemplateLoader(emailConfigProperties.templatePrefix, emailConfigProperties.templateSuffix)
        )

        var text: String? = null
        val textTemplatePath = textTemplatePath(data)
        if (textTemplatePath != null) {
            val textTemplate = mustache.loadTemplate(textTemplatePath)
            text = textTemplate.execute(data)
        }

        var html: String? = null
        val htmlTemplatePath = htmlTemplatePath(data)
        if (htmlTemplatePath != null) {
            val htmlTemplate = mustache.loadTemplate(htmlTemplatePath)
            html = htmlTemplate.execute(data)
        }

        if (text != null && html != null) {
            helper.setText(text, html)
        } else if (text != null) {
            helper.setText(text)
        } else if (html != null) {
            helper.setText(html, true)
        }

        for (attachment in attachments(data)) {
            val template = mustache.loadTemplate(attachment.path(data))
            val content = template.execute(data)
            helper.addAttachment(attachment.name(data)) { ByteArrayInputStream(content.encodeToByteArray()) }
        }
    }

    protected abstract inner class TemplateAttachment {
        abstract fun path(data: T?): String
        abstract fun name(data: T?): String
    }
}
