package pt.up.fe.ni.website.backend.email

import jakarta.activation.DataSource
import jakarta.activation.FileDataSource
import jakarta.activation.URLDataSource
import java.io.File
import java.net.URL
import org.springframework.mail.javamail.MimeMessageHelper

class SimpleEmailBuilder : BaseEmailBuilder() {
    private var text: String? = null
    private var html: String? = null
    private var subject: String? = null
    private var attachments: MutableList<EmailFile> = mutableListOf()
    // Inlines - similar to attachments, not shown as downloadable but can be inserted in an email. For example, inline images.
    private var inlines: MutableList<EmailFile> = mutableListOf()

    fun text(text: String) = apply {
        this.text = text
    }

    fun html(html: String) = apply {
        this.html = html
    }

    fun subject(subject: String) = apply {
        this.subject = subject
    }

    fun attach(name: String, content: DataSource) = apply {
        attachments.add(EmailFile(name, content))
    }

    fun attach(name: String, content: File) = apply {
        attachments.add(EmailFile(name, FileDataSource(content)))
    }

    fun attach(name: String, path: String) = apply {
        attachments.add(EmailFile(name, URLDataSource(URL(path))))
    }

    fun inline(name: String, content: DataSource) = apply {
        inlines.add(EmailFile(name, content))
    }

    fun inline(name: String, content: File) = apply {
        inlines.add(EmailFile(name, FileDataSource(content)))
    }

    fun inline(name: String, path: String) = apply {
        inlines.add(EmailFile(name, URLDataSource(URL(path))))
    }

    override fun build(helper: MimeMessageHelper) {
        super.build(helper)

        when {
            text != null && html != null -> helper.setText(text!!, html!!)
            html != null -> helper.setText(html!!, true)
            text != null -> helper.setText(text!!)
        }

        subject?.let { helper.setSubject(it) }

        attachments.forEach { helper.addAttachment(it.name, it.content) }
        inlines.forEach { helper.addInline(it.name, it.content) }
    }

    private data class EmailFile(
        val name: String,
        val content: DataSource
    )
}
