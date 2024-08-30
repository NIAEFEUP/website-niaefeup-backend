package pt.up.fe.ni.website.backend.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.email.EmailBuilder

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    fun send(email: EmailBuilder) {
        val message = mailSender.createMimeMessage()

        val helper = MimeMessageHelper(message, true)
        email.build(helper)

        mailSender.send(message)
    }
}
