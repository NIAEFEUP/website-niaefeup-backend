package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import java.util.Calendar
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AccountControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: AccountRepository
) {
    val testAccount = Account(
        "Test Account",
        "test_account@test.com",
        "test_password",
        "This is a test account",
        TestUtils.createDate(2001, Calendar.JULY, 28),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        )
    )

    @Nested
    @DisplayName("GET /accounts")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAllAccounts {
        private val testAccounts = listOf(
            testAccount,
            Account(
                "Test Account 2",
                "test_account2@test.com",
                "test_password",
                null,
                null,
                null,
                null,
                null,
                emptyList()
            )
        )

        @BeforeAll
        fun addAccounts() {
            for (account in testAccounts) repository.save(account)
        }

        @Test
        fun `should return all accounts`() {
            mockMvc.get("/accounts")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { json(objectMapper.writeValueAsString(testAccounts)) }
                }
        }
    }

    @Nested
    @DisplayName("GET /accounts/{id}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAccount {
        @BeforeAll
        fun addAccount() {
            repository.save(testAccount)
        }

        @Test
        fun `should return the account`() {
            mockMvc.get("/accounts/${testAccount.id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.name") { value(testAccount.name) }
                    jsonPath("$.email") { value(testAccount.email) }
                    jsonPath("$.bio") { value(testAccount.bio) }
                    jsonPath("$.birthDate") { value(testAccount.birthDate.toJson()) }
                    jsonPath("$.photoPath") { value(testAccount.photoPath) }
                    jsonPath("$.linkedin") { value(testAccount.linkedin) }
                    jsonPath("$.github") { value(testAccount.github) }
                    jsonPath("$.websites.length()") { value(1) }
                    jsonPath("$.websites[0].url") { value(testAccount.websites[0].url) }
                    jsonPath("$.websites[0].iconPath") { value(testAccount.websites[0].iconPath) }
                }
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.get("/accounts/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("account not found with id 1234") }
            }
        }
    }

    @Nested
    @DisplayName("POST /accounts/new")
    inner class CreateAccount {
        @AfterEach
        fun clearAccounts() {
            repository.deleteAll()
        }

        @Test
        fun `should create the account`() {
            mockMvc.post("/accounts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = testAccount.toJson()
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.name") { value(testAccount.name) }
                jsonPath("$.email") { value(testAccount.email) }
                jsonPath("$.bio") { value(testAccount.bio) }
                jsonPath("$.birthDate") { value(testAccount.birthDate.toJson()) }
                jsonPath("$.photoPath") { value(testAccount.photoPath) }
                jsonPath("$.linkedin") { value(testAccount.linkedin) }
                jsonPath("$.github") { value(testAccount.github) }
                jsonPath("$.websites.length()") { value(1) }
                jsonPath("$.websites[0].url") { value(testAccount.websites[0].url) }
                jsonPath("$.websites[0].iconPath") { value(testAccount.websites[0].iconPath) }
            }
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.post("/accounts/new") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "name" to testAccount.name,
                    "email" to testAccount.email,
                    "password" to testAccount.password,
                    "websites" to emptyList<CustomWebsite>()
                )
            )

            @Nested
            @DisplayName("name")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class NameValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "name"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Name.minSize} and ${Constants.Name.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Name.minSize, Constants.Name.maxSize)
            }

            @Nested
            @DisplayName("email")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class EmailValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "email"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should not be empty`() = validationTester.isNotEmpty()

                @Test
                fun `should be a valid email`() = validationTester.isEmail()
            }

            @Nested
            @DisplayName("password")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class PasswordValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "password"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Password.minSize} and ${Constants.Password.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Password.minSize, Constants.Password.maxSize)
            }

            @Nested
            @DisplayName("bio")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class BioValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "bio"
                }

                @Test
                @DisplayName("size should be between ${Constants.Bio.minSize} and ${Constants.Bio.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Bio.minSize, Constants.Bio.maxSize)
            }

            @Nested
            @DisplayName("birthDate")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class BirthDateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "birthDate"
                }

                @Test
                fun `should be a valid date`() = validationTester.isDate()

                @Test
                fun `should be in the past`() = validationTester.isPastDate()
            }

            @Nested
            @DisplayName("photoPath")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class PhotoPathValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "photoPath"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be URL`() = validationTester.isUrl()
            }

            @Nested
            @DisplayName("linkedin")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class LinkedinValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "linkedin"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be URL`() = validationTester.isUrl()
            }

            @Nested
            @DisplayName("github")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class GithubValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "github"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be URL`() = validationTester.isUrl()
            }

            @Nested
            @DisplayName("websites")
            inner class WebsitesValidation {
                private val validationTester = ValidationTester(
                    req = { params: Map<String, Any?> ->
                        mockMvc.post("/accounts/new") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                mapOf(
                                    "name" to testAccount.name,
                                    "email" to testAccount.email,
                                    "password" to testAccount.password,
                                    "websites" to listOf<Any>(params)
                                )
                            )
                        }
                    },
                    requiredFields = mapOf(
                        "url" to "https://www.google.com"
                    )
                )

                @Nested
                @DisplayName("url")
                inner class UrlValidation {
                    @BeforeEach
                    fun setParam() {
                        validationTester.param = "url"
                    }

                    @Test
                    fun `should be required`() = validationTester.isRequired()

                    @Test
                    fun `should not be empty`() {
                        validationTester.parameterName = "websites[0].url"
                        validationTester.isNotEmpty()
                    }

                    @Test
                    fun `should be URL`() {
                        validationTester.parameterName = "websites[0].url"
                        validationTester.isUrl()
                    }
                }

                @Nested
                @DisplayName("iconPath")
                inner class IconPathValidation {
                    @BeforeEach
                    fun setParam() {
                        validationTester.param = "iconPath"
                    }

                    @Test
                    fun `should be bull or not blank`() {
                        validationTester.parameterName = "websites[0].iconPath"
                        validationTester.isNullOrNotBlank()
                    }

                    @Test
                    fun `should be URL`() {
                        validationTester.parameterName = "websites[0].iconPath"
                        validationTester.isUrl()
                    }
                }
            }
        }

        @Test
        fun `should fail to create account with existing email`() {
            println("testAccount: ${objectMapper.writeValueAsString(testAccount)}")
            mockMvc.post("/accounts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = testAccount.toJson()
            }.andExpect { status { isOk() } }

            mockMvc.post("/accounts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = testAccount.toJson()
            }.andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("email already exists") }
            }
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }

    fun Account?.toJson(): String {
        // password is ignored on serialization, so add it manually
        // for account creation test cases
        return objectMapper.writeValueAsString(
            objectMapper.convertValue(this, Map::class.java).plus(
                mapOf(
                    "password" to this?.password
                )
            )
        )
    }
}
