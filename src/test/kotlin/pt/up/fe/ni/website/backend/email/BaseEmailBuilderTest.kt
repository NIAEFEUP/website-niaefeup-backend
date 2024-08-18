import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import pt.up.fe.ni.website.backend.config.email.EmailConfigProperties
import pt.up.fe.ni.website.backend.email.BaseEmailBuilder
import pt.up.fe.ni.website.backend.model.Account

@ExtendWith(MockitoExtension::class)
class BaseEmailBuilderTest {
    private lateinit var emailConfigProperties: EmailConfigProperties
    private lateinit var mimeMessageHelper: MimeMessageHelper
    private lateinit var baseEmailBuilder: BaseEmailBuilderImpl

    @BeforeEach
    fun setup() {
        emailConfigProperties = Mockito.mock(EmailConfigProperties::class.java).apply {
            Mockito.`when`(from).thenReturn("test@email.com")
            Mockito.`when`(fromPersonal).thenReturn("Test")
        }

        val javaMailSender = JavaMailSenderImpl()
        val mimeMessage = javaMailSender.createMimeMessage()
        mimeMessageHelper = MimeMessageHelper(mimeMessage, true)

        baseEmailBuilder = BaseEmailBuilderImpl(emailConfigProperties)
    }

    @Test
    fun `valid emails are correctly set in 'to' field`() {
        baseEmailBuilder.to("to1@email.com", "to2@email.com")
        baseEmailBuilder.build(mimeMessageHelper)

        Assertions.assertEquals(setOf("to1@email.com", "to2@email.com"), baseEmailBuilder.getToEmails())
    }

    @Test
    fun `invalid emails throw exception`() {
        Assertions.assertThrows(ConstraintViolationException::class.java) {
            baseEmailBuilder.to("invalid")
        }
    }

    @Test
    fun `valid account emails are correctly set in 'to' field`() {
        val account1 = Account("Account 1", "account1@email.com","account1password", null, null, null, null, null)
        val account2 = Account("Account 2", "account2@email.com", "account2password", null, null, null, null, null)

        baseEmailBuilder.to(account1, account2)
        baseEmailBuilder.build(mimeMessageHelper)

        Assertions.assertEquals(setOf("account1@email.com", "account2@email.com"), baseEmailBuilder.getToEmails())
    }

    @Test
    fun `emails are correctly set in 'cc' field`() {
        baseEmailBuilder.cc("cc1@email.com", "cc2@email.com")
        baseEmailBuilder.build(mimeMessageHelper)

        Assertions.assertEquals(setOf("cc1@email.com", "cc2@email.com"), baseEmailBuilder.getCcEmails())
    }

    @Test
    fun `emails are correctly set in 'bcc' field`() {
        baseEmailBuilder.bcc("bcc1@email.com", "bcc2@email.com")
        baseEmailBuilder.build(mimeMessageHelper)

        Assertions.assertEquals(setOf("bcc1@email.com", "bcc2@email.com"), baseEmailBuilder.getBccEmails())
    }

    @Test
    fun `'from' email and personal name are set correctly`() {
        baseEmailBuilder.from("from@email.com", "From")
        baseEmailBuilder.build(mimeMessageHelper)

        Assertions.assertEquals("from@email.com", baseEmailBuilder.getFromEmail())
        Assertions.assertEquals("From", baseEmailBuilder.getName())
    }
}

class BaseEmailBuilderImpl(
    override val emailConfigProperties: EmailConfigProperties
) : BaseEmailBuilder() {
    fun getToEmails(): Set<String> = to
    fun getCcEmails(): Set<String> = cc
    fun getBccEmails(): Set<String> = bcc
    fun getFromEmail(): String? = from
    fun getName(): String? = fromPersonal
}
