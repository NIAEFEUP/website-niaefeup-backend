package pt.up.fe.ni.website.backend.email

import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

class SimpleEmailBuilder : BaseEmailBuilder() {
    private var text: String? = null
    private var html: String? = null
    private var subject: String? = null

    fun text(text: String) = apply {
        this.text = text
    }

    fun html(html: String) = apply {
        this.html = html
    }

    fun subject(subject: String) = apply {
        this.subject = subject
    }

    override fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties) {
        super.build(helper, emailConfigProperties)

        if (text != null && html != null) {
            helper.setText(text!!, html!!)
        } else if (text != null) {
            helper.setText(text!!)
        } else if (html != null) {
            helper.setText(html!!, true)
        }

        if (subject != null) {
            helper.setSubject(subject!!)
        }
    }
}
