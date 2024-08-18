package pt.up.fe.ni.website.backend.email

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.ApplicationContextUtils
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties
import pt.up.fe.ni.website.backend.model.Account

abstract class BaseEmailBuilder : EmailBuilder {
    protected open val emailConfigProperties = ApplicationContextUtils.getBean(EmailConfigProperties::class.java)

    var from: String? = null
    var fromPersonal: String? = null
    var to: MutableSet<String> = mutableSetOf()
    var cc: MutableSet<String> = mutableSetOf()
    var bcc: MutableSet<String> = mutableSetOf()

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

    override fun build(helper: MimeMessageHelper) {
        helper.setFrom(from ?: emailConfigProperties.from, fromPersonal ?: emailConfigProperties.fromPersonal)

        to.forEach(helper::setTo)
        cc.forEach(helper::setCc)
        bcc.forEach(helper::setBcc)
    }
}
