package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockPart
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.config.upload.UploadConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAccount
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentCustomRequestSchemaEmptyResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentCustomRequestSchemaErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema
import pt.up.fe.ni.website.backend.utils.mockmvc.multipartBuilder

@ControllerTest
class AccountControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: AccountRepository,
    val encoder: PasswordEncoder,
    val uploadConfigProperties: UploadConfigProperties
) {
    val documentation: ModelDocumentation = PayloadAccount()

    val testAccount = Account(
        "Test Account",
        "test_account@test.com",
        "test_password",
        "This is a test account",
        TestUtils.createDate(2001, Calendar.JULY, 28),
        null,
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        ),
        mutableListOf()
    )

    @NestedTest
    @DisplayName("GET /accounts")
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
                emptyList(),
                mutableListOf()
            )
        )

        @BeforeEach
        fun addAccounts() {
            for (account in testAccounts) repository.save(account)
        }

        @Test
        fun `should return all accounts`() {
            mockMvc.perform(get("/accounts"))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    content().json(objectMapper.writeValueAsString(testAccounts))
                )
                .andDocument(
                    documentation.getModelDocumentationArray(),
                    "Get all the accounts",
                    "The operation returns an array of accounts, allowing to easily retrieve all " +
                        "the created accounts."
                )
        }
    }

    @NestedTest
    @DisplayName("GET /accounts/{id}")
    inner class GetAccount {
        @BeforeEach
        fun addAccount() {
            repository.save(testAccount)
        }

        private val parameters = listOf(
            parameterWithName("id").description(
                "ID of the account to retrieve"
            )
        )

        @Test
        fun `should return the account`() {
            mockMvc.perform(get("/accounts/{id}", testAccount.id))
                .andExpect {
                    status().isOk
                    content().contentType(MediaType.APPLICATION_JSON)
                    jsonPath("$.name").value(testAccount.name)
                    jsonPath("$.email").value(testAccount.email)
                    jsonPath("$.bio").value(testAccount.bio)
                    jsonPath("$.birthDate").value(testAccount.birthDate.toJson())
                    jsonPath("$.linkedin").value(testAccount.linkedin)
                    jsonPath("$.github").value(testAccount.github)
                    jsonPath("$.websites.length()").value(1)
                    jsonPath("$.websites[0].url").value(testAccount.websites[0].url)
                    jsonPath("$.websites[0].iconPath").value(testAccount.websites[0].iconPath)
                }
                .andDocument(
                    documentation,
                    "Get accounts by ID",
                    "This endpoint allows the retrieval of a single account using its ID.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.perform(get("/accounts/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
                )
        }
    }

    @NestedTest
    @DisplayName("POST /accounts/new")
    inner class CreateAccount {
        @AfterEach
        fun clearAccounts() {
            repository.deleteAll()
        }

        @Test
        fun `should create the account`() {
            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(testAccount.name),
                    jsonPath("$.email").value(testAccount.email),
                    jsonPath("$.bio").value(testAccount.bio),
                    jsonPath("$.birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.linkedin").value(testAccount.linkedin),
                    jsonPath("$.github").value(testAccount.github),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.websites[0].iconPath").value(testAccount.websites[0].iconPath)
                )
        }

        @Test
        fun `should create an account with an empty website list`() {
            val noWebsite = Account(
                "Test Account",
                "no_website@email.com",
                "test_password",
                "This is a test account",
                TestUtils.createDate(2001, Calendar.JULY, 28),
                "https://test-photo.com",
                "https://linkedin.com",
                "https://github.com"
            )

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to noWebsite.name,
                    "email" to noWebsite.email,
                    "password" to noWebsite.password,
                    "bio" to noWebsite.bio,
                    "birthDate" to noWebsite.birthDate,
                    "linkedin" to noWebsite.linkedin,
                    "github" to noWebsite.github
                )
            )

            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", data)
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(noWebsite.name),
                    jsonPath("$.email").value(noWebsite.email),
                    jsonPath("$.bio").value(noWebsite.bio),
                    jsonPath("$.birthDate").value(noWebsite.birthDate.toJson()),
                    jsonPath("$.linkedin").value(noWebsite.linkedin),
                    jsonPath("$.github").value(noWebsite.github),
                    jsonPath("$.websites.length()").value(0)
                )
        }

        @Test
        fun `should create the account with valid image`() {
            val uuid: UUID = UUID.randomUUID()
            val mockedSettings = Mockito.mockStatic(UUID::class.java)
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)

            val expectedPhotoPath = "${uploadConfigProperties.staticServe}/profile/${testAccount.email}-$uuid.jpeg"

            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .addFile()
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(testAccount.name),
                    jsonPath("$.email").value(testAccount.email),
                    jsonPath("$.bio").value(testAccount.bio),
                    jsonPath("$.birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.linkedin").value(testAccount.linkedin),
                    jsonPath("$.github").value(testAccount.github),
                    jsonPath("$.photo").value(expectedPhotoPath),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.websites[0].iconPath").value(testAccount.websites[0].iconPath)
                )

            mockedSettings.close()
        }

        @Test
        fun `should fail to create account with invalid filename extension`() {
            val uuid: UUID = UUID.randomUUID()
            val mockedSettings = Mockito.mockStatic(UUID::class.java)
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)

            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .addFile(filename = "photo.pdf", contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg or jpeg)"),
                    jsonPath("$.errors[0].param").value("createAccount.photo")
                )

            mockedSettings.close()
        }

        @Test
        fun `should fail to create account with invalid filename media type`() {
            val uuid: UUID = UUID.randomUUID()
            val mockedSettings = Mockito.mockStatic(UUID::class.java)
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)

            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .addFile(contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg or jpeg)"),
                    jsonPath("$.errors[0].param").value("createAccount.photo")
                )

            mockedSettings.close()
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.multipartBuilder("/accounts/new")
                        .addPart("dto", objectMapper.writeValueAsString(params))
                        .perform()
                },
                requiredFields = mapOf(
                    "name" to testAccount.name,
                    "email" to testAccount.email,
                    "password" to testAccount.password,
                    "websites" to emptyList<CustomWebsite>()
                )
            )

            @NestedTest
            @DisplayName("name")
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

            @NestedTest
            @DisplayName("email")
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

            @NestedTest
            @DisplayName("password")
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

            @NestedTest
            @DisplayName("bio")
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

            @NestedTest
            @DisplayName("birthDate")
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

            @NestedTest
            @DisplayName("linkedin")
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

            @NestedTest
            @DisplayName("github")
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

            @NestedTest
            @DisplayName("websites")
            inner class WebsitesValidation {
                private val validationTester = ValidationTester(
                    req = { params: Map<String, Any?> ->
                        val accountPart = MockPart(
                            "dto",
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "name" to testAccount.name,
                                    "email" to testAccount.email,
                                    "password" to testAccount.password,
                                    "websites" to listOf<Any>(params)
                                )
                            ).toByteArray()
                        )
                        accountPart.headers.contentType = MediaType.APPLICATION_JSON

                        mockMvc.perform(multipart("/accounts/new").part(accountPart))
                            .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                    },
                    requiredFields = mapOf(
                        "url" to "https://www.google.com"
                    )
                )

                @NestedTest
                @DisplayName("url")
                inner class UrlValidation {
                    @BeforeAll
                    fun setParam() {
                        validationTester.param = "url"
                    }

                    @Test
                    fun `should be required`() {
                        validationTester.parameterName = "url"
                        validationTester.isRequired()
                    }

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

                @NestedTest
                @DisplayName("iconPath")
                inner class IconPathValidation {
                    @BeforeAll
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
            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .perform()
                .andExpect(status().isOk)

            mockMvc.multipartBuilder("/accounts/new")
                .addPart("dto", testAccount.toJson())
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("email already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }
    }

    @NestedTest
    @DisplayName("POST /accounts/changePassword/{id}")
    inner class ChangePassword {
        private val password = "test_password"
        private val changePasswordAccount: Account = ObjectMapper().readValue(
            ObjectMapper().writeValueAsString(testAccount),
            Account::class.java
        )

        init {
            changePasswordAccount.password = encoder.encode(changePasswordAccount.password)
        }

        private val parameters = listOf(
            parameterWithName("id").description(
                "ID of the account to change the password."
            )
        )

        private val passwordChangePayload = PayloadSchema(
            "password-change",
            mutableListOf(
                DocumentedJSONField("oldPassword", "Current account password", JsonFieldType.STRING),
                DocumentedJSONField("newPassword", "New account password", JsonFieldType.STRING)
            )
        )

        @BeforeEach
        fun addAccount() {
            repository.save(changePasswordAccount)
        }

        @Test
        fun `should change password`() {
            mockMvc.perform(
                post("/accounts/changePassword/{id}", changePasswordAccount.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "oldPassword" to password,
                                "newPassword" to "test_password2"
                            )
                        )
                    )
            ).andExpectAll(status().isOk)
                .andDocumentCustomRequestSchemaEmptyResponse(
                    documentation,
                    passwordChangePayload,
                    "Change account password",
                    "Replaces sets a new account password",
                    urlParameters = parameters,
                    documentRequestPayload = true
                )

            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "email" to changePasswordAccount.email,
                        "password" to "test_password2"
                    )
                )
            }.andExpect { status { isOk() } }
        }

        @Test
        fun `should fail due to wrong password`() {
            mockMvc.perform(
                post("/accounts/changePassword/{id}", changePasswordAccount.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "oldPassword" to "wrong_password",
                                "newPassword" to "test_password2"
                            )
                        )
                    )
            ).andExpectAll(status().isUnprocessableEntity)
                .andDocumentCustomRequestSchemaErrorResponse(
                    documentation,
                    passwordChangePayload,
                    urlParameters = parameters,
                    hasRequestPayload = true
                )
        }
    }

    @NestedTest
    @DisplayName("DELETE /accounts/{accountId}")
    inner class DeleteAccount {
        @BeforeEach
        fun addAccount() {
            repository.save(testAccount)
        }

        private val parameters = listOf(parameterWithName("id").description("ID of the account to delete"))

        @Test
        fun `should delete the account`() {
            mockMvc.perform(delete("/accounts/{id}", testAccount.id)).andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$").isEmpty
            )
                .andDocumentEmptyObjectResponse(
                    documentation,
                    "Delete accounts",
                    "This operation deletes an account using its ID.",
                    urlParameters = parameters
                )

            assert(repository.findById(testAccount.id!!).isEmpty)
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.perform(delete("/accounts/{id}", 1234)).andExpectAll(

                status().isNotFound,
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors[0].message").value("account not found with id 1234")
            )
                .andDocumentErrorResponse(
                    documentation,
                    urlParameters = parameters
                )
        }
    }

    @NestedTest
    @DisplayName("PUT /accounts/{accountId}")
    inner class UpdateAccount {
        private val newAccount = Account(
            "Another test Account",
            "test2_account@test.com",
            "test_password",
            "This is another test account",
            TestUtils.createDate(2003, Calendar.APRIL, 4),
            "https://test-photo.com",
            "https://linkedin.com",
            "https://github.com",
            listOf(
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
            )
        )

        @BeforeEach
        fun addAccounts() {
            repository.save(testAccount)
            repository.save(newAccount)
        }

        private val documentation = PayloadAccount(includePassword = false)
        private val parameters = listOf(parameterWithName("id").description("ID of the account to update"))

        @Test
        fun `should update the account`() {
            val newName = "Test Account 2"
            val newEmail = "test_account2@test.com"
            val newBio = "This is a test account altered"
            val newBirthDate = TestUtils.createDate(2003, Calendar.JULY, 28)
            val newLinkedin = "https://linkedin2.com"
            val newGithub = "https://github2.com"
            val newWebsites = listOf(
                CustomWebsite("https://test-website2.com", "https://test-website.com/logo.png")
            )

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to newName,
                    "email" to newEmail,
                    "bio" to newBio,
                    "birthDate" to newBirthDate,
                    "linkedin" to newLinkedin,
                    "github" to newGithub,
                    "websites" to newWebsites
                )
            )

            mockMvc.multipartBuilder("/accounts/${testAccount.id}")
                .addPart("dto", data)
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(newName),
                    jsonPath("$.email").value(newEmail),
                    jsonPath("$.bio").value(newBio),
                    jsonPath("$.birthDate").value(newBirthDate.toJson()),
                    jsonPath("$.linkedin").value(newLinkedin),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(newWebsites[0].url),
                    jsonPath("$.websites[0].iconPath").value(newWebsites[0].iconPath)
                )
//                .andDocument(
//                    documentation,
//                    "Update accounts",
//                    "Update a previously created account, with the exception of its password, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//            )

            val updatedAccount = repository.findById(testAccount.id!!).get()
            Assertions.assertEquals(newName, updatedAccount.name)
            Assertions.assertEquals(newEmail, updatedAccount.email)
            Assertions.assertEquals(newBio, updatedAccount.bio)
            Assertions.assertEquals(newBirthDate.toJson(), updatedAccount.birthDate.toJson())
            Assertions.assertEquals(newLinkedin, updatedAccount.linkedin)
            Assertions.assertEquals(newWebsites[0].url, updatedAccount.websites[0].url)
            Assertions.assertEquals(newWebsites[0].iconPath, updatedAccount.websites[0].iconPath)
        }

        @Test
        fun `should update the account when email is unchanged`() {
            val newName = "Test Account 2"
            val newBio = "This is a test account with no altered email"
            val newBirthDate = TestUtils.createDate(2003, Calendar.JULY, 28)
            val newLinkedin = "https://linkedin2.com"
            val newGithub = "https://github2.com"
            val newWebsites = listOf(
                CustomWebsite("https://test-website2.com", "https://test-website.com/logo.png")
            )

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to newName,
                    "email" to testAccount.email,
                    "bio" to newBio,
                    "birthDate" to newBirthDate,
                    "linkedin" to newLinkedin,
                    "github" to newGithub,
                    "websites" to newWebsites
                )
            )

            mockMvc.multipartBuilder("/accounts/${testAccount.id}")
                .addPart("dto", data)
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(newName),
                    jsonPath("$.email").value(testAccount.email),
                    jsonPath("$.bio").value(newBio),
                    jsonPath("$.birthDate").value(newBirthDate.toJson()),
                    jsonPath("$.linkedin").value(newLinkedin),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(newWebsites[0].url),
                    jsonPath("$.websites[0].iconPath").value(newWebsites[0].iconPath)
                )
//                .andDocument(
//                    documentation,
//                    "Update accounts",
//                    "Update a previously created account, with the exception of its password, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//            )

            val updatedAccount = repository.findById(testAccount.id!!).get()
            Assertions.assertEquals(newName, updatedAccount.name)
            Assertions.assertEquals(testAccount.email, updatedAccount.email)
            Assertions.assertEquals(newBio, updatedAccount.bio)
            Assertions.assertEquals(newBirthDate.toJson(), updatedAccount.birthDate.toJson())
            Assertions.assertEquals(newLinkedin, updatedAccount.linkedin)
            Assertions.assertEquals(newWebsites[0].url, updatedAccount.websites[0].url)
            Assertions.assertEquals(newWebsites[0].iconPath, updatedAccount.websites[0].iconPath)
        }

        @Test
        fun `should update the account with valid image`() {
            val uuid: UUID = UUID.randomUUID()
            val mockedSettings = Mockito.mockStatic(UUID::class.java)
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)

            val newName = "Test Account 2"
            val newEmail = "test_account2@test.com"
            val newBio = "This is a test account altered"
            val newBirthDate = TestUtils.createDate(2003, Calendar.JULY, 28)
            val newLinkedin = "https://linkedin2.com"
            val newGithub = "https://github2.com"
            val newWebsites = listOf(
                CustomWebsite("https://test-website2.com", "https://test-website.com/logo.png")
            )

            val expectedPhotoPath = "${uploadConfigProperties.staticServe}/profile/$newEmail-$uuid.jpeg"

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to newName,
                    "email" to newEmail,
                    "bio" to newBio,
                    "birthDate" to newBirthDate,
                    "linkedin" to newLinkedin,
                    "github" to newGithub,
                    "websites" to newWebsites
                )
            )

            mockMvc.multipartBuilder("/accounts/${testAccount.id}")
                .asPutMethod()
                .addPart("dto", data)
                .addFile()
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(newName),
                    jsonPath("$.email").value(newEmail),
                    jsonPath("$.bio").value(newBio),
                    jsonPath("$.birthDate").value(newBirthDate.toJson()),
                    jsonPath("$.photo").value(expectedPhotoPath),
                    jsonPath("$.linkedin").value(newLinkedin),
                    jsonPath("$.github").value(newGithub),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(newWebsites[0].url),
                    jsonPath("$.websites[0].iconPath").value(newWebsites[0].iconPath)
                )
//                .andDocument(
//                    documentation,
//                    "Update accounts",
//                    "Update a previously created account, with the exception of its password, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//            )

            val updatedAccount = repository.findById(testAccount.id!!).get()
            Assertions.assertEquals(newName, updatedAccount.name)
            Assertions.assertEquals(newEmail, updatedAccount.email)
            Assertions.assertEquals(expectedPhotoPath, updatedAccount.photo)
            Assertions.assertEquals(newBio, updatedAccount.bio)
            Assertions.assertEquals(newBirthDate.toJson(), updatedAccount.birthDate.toJson())
            Assertions.assertEquals(newLinkedin, updatedAccount.linkedin)
            Assertions.assertEquals(newWebsites[0].url, updatedAccount.websites[0].url)
            Assertions.assertEquals(newWebsites[0].iconPath, updatedAccount.websites[0].iconPath)

            mockedSettings.close()
        }

        @Test
        fun `should fail if the account does not exist`() {
            val newName = "Test Account 2"
            val newEmail = "test_account2@test.com"
            val newBio = "This is a test account altered"
            val newBirthDate = TestUtils.createDate(2003, Calendar.JULY, 28)
            val newPhotoPath = "https://test-photo2.com"
            val newLinkedin = "https://linkedin2.com"
            val newGithub = "https://github2.com"
            val newWebsites = listOf(
                CustomWebsite("https://test-website2.com", "https://test-website.com/logo.png")
            )

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to newName,
                    "email" to newEmail,
                    "bio" to newBio,
                    "birthDate" to newBirthDate,
                    "photoPath" to newPhotoPath,
                    "linkedin" to newLinkedin,
                    "github" to newGithub,
                    "websites" to newWebsites
                )
            )

            mockMvc.multipartBuilder("/accounts/${1234}")
                .addPart("dto", data)
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
//                .andDocumentErrorResponse(
//                    documentation,
//                    urlParameters = parameters,
//                    hasRequestPayload = true
//                )
        }

        @Test
        fun `should fail if the new email is already taken`() {
            val newName = "Test Account 2"
            val newBio = "This is a test account altered"
            val newBirthDate = TestUtils.createDate(2003, Calendar.JULY, 28)
            val newPhotoPath = "https://test-photo2.com"
            val newLinkedin = "https://linkedin2.com"
            val newGithub = "https://github2.com"
            val newWebsites = listOf(
                CustomWebsite("https://test-website2.com", "https://test-website.com/logo.png")
            )

            val data = objectMapper.writeValueAsString(
                mapOf(
                    "name" to newName,
                    "email" to "test2_account@test.com",
                    "bio" to newBio,
                    "birthDate" to newBirthDate,
                    "photoPath" to newPhotoPath,
                    "linkedin" to newLinkedin,
                    "github" to newGithub,
                    "websites" to newWebsites
                )
            )

            mockMvc.multipartBuilder("/accounts/${testAccount.id}")
                .addPart("dto", data)
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("email already exists")
                )
//                .andDocumentErrorResponse(
//                    documentation,
//                    urlParameters = parameters,
//                    hasRequestPayload = true
//                )
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
