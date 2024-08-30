package pt.up.fe.ni.website.backend.email

import org.springframework.mail.javamail.MimeMessageHelper

interface EmailBuilder {
    fun build(helper: MimeMessageHelper)
}
