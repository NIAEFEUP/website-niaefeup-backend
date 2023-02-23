package pt.up.fe.ni.website.backend.email

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties
import pt.up.fe.ni.website.backend.model.Account

abstract class BaseEmailBuilder : EmailBuilder {
    private var from: String? = null
    private var fromPersonal: String? = null
    private var to: MutableSet<String> = mutableSetOf()
    private var cc: MutableSet<String> = mutableSetOf()
    private var bcc: MutableSet<String> = mutableSetOf()

    fun from(@Email email: String) = apply {
        from = email
    }

    fun fromPersonal(name: String) = apply {
        fromPersonal = name
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

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        if (from == null) {
            helper.setFrom(emailConfigProperties.from, emailConfigProperties.fromPersonal ?: emailConfigProperties.from)
        } else if (fromPersonal == null) {
            helper.setFrom(from!!)
        } else {
            helper.setFrom(from!!, fromPersonal!!)
        }

        to.forEach(helper::setTo)
        cc.forEach(helper::setCc)
        bcc.forEach(helper::setBcc)
    }
}
