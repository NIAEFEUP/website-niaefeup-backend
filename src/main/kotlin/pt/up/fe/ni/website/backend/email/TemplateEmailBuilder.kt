package pt.up.fe.ni.website.backend.email

import com.samskivert.mustache.Mustache
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

abstract class TemplateEmailBuilder<T> : BaseEmailBuilder() {
    private var data: T? = null

    open val htmlTemplatePath: String? = null
    open val textTemplatePath: String? = null

    protected open fun subject(data: T?): String = ""

    fun data(data: T) = apply {
        this.data = data
    }

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        super.build(helper, emailConfigProperties)

        helper.setSubject(subject(data))

        val mustache = Mustache.compiler().withLoader(
            MustacheResourceTemplateLoader("classpath:/templates/email/", ".mustache")
        )
        var text: String? = null
        var html: String? = null

        if (textTemplatePath != null) {
            val textTemplate = mustache.loadTemplate(textTemplatePath)
            text = textTemplate.execute(data)
        }

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
    }
}
