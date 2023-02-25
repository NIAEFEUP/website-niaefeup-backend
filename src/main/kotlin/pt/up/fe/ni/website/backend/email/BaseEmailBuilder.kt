package pt.up.fe.ni.website.backend.email

import jakarta.activation.DataSource
import jakarta.activation.FileDataSource
import jakarta.activation.URLDataSource
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import java.io.File
import java.net.URL
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties
import pt.up.fe.ni.website.backend.model.Account

abstract class BaseEmailBuilder : EmailBuilder {
    private var from: String? = null
    private var fromPersonal: String? = null
    private var to: MutableSet<String> = mutableSetOf()
    private var cc: MutableSet<String> = mutableSetOf()
    private var bcc: MutableSet<String> = mutableSetOf()
    private var attachments: MutableList<Attachment> = mutableListOf()

    fun from(@Email email: String, personal: String = email) = apply {
        from = email
        fromPersonal = personal
    }

    fun to(@Email vararg emails: String) = apply {
        to.addAll(emails)
    }

    fun to(@Valid vararg users: Account) = apply {
        to.addAll(users.map { it.email })
    }

    fun cc(@Email vararg emails: String) = apply {
        cc.addAll(emails)
    }

    fun cc(@Valid vararg users: Account) = apply {
        cc.addAll(users.map { it.email })
    }

    fun bcc(@Email vararg emails: String) = apply {
        bcc.addAll(emails)
    }

    fun bcc(@Valid vararg users: Account) = apply {
        bcc.addAll(users.map { it.email })
    }

    fun attach(name: String, content: DataSource) = apply {
        attachments.add(Attachment(name, content))
    }

    fun attach(name: String, content: File) = apply {
        attachments.add(Attachment(name, FileDataSource(content)))
    }

    fun attach(name: String, path: String) = apply {
        attachments.add(Attachment(name, URLDataSource(URL(path))))
    }

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        helper.setFrom(from ?: emailConfigProperties.from, fromPersonal ?: emailConfigProperties.fromPersonal)

        to.forEach(helper::setTo)
        cc.forEach(helper::setCc)
        bcc.forEach(helper::setBcc)

        attachments.forEach { helper.addAttachment(it.name, it.content) }
    }

    protected data class Attachment(
        val name: String,
        val content: DataSource
    )
}
