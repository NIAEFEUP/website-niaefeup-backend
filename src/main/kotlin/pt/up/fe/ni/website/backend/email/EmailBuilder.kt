package pt.up.fe.ni.website.backend.email

import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties

interface EmailBuilder {
    fun build(helper: MimeMessageHelper, emailConfigProperties: EmailConfigProperties)
}
