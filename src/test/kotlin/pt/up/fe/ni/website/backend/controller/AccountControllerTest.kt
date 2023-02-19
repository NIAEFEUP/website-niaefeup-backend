package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema
import java.util.Calendar
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.AccountConstants as Constants

@ControllerTest
@AutoConfigureRestDocs
class AccountControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: AccountRepository,
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png"),
        ),
        emptyList(),
    )

    private val accountFields = listOf<FieldDescriptor>(
        fieldWithPath("name").type(JsonFieldType.STRING).description("Name of the account owner"),
        fieldWithPath("email").type(JsonFieldType.STRING).description("Email associated to the account"),
        fieldWithPath("bio").type(JsonFieldType.STRING).description("Short profile description").optional(),
        fieldWithPath("birthDate").type(JsonFieldType.STRING).description("Birth date of the owner").optional(),
        fieldWithPath("photoPath").type(JsonFieldType.STRING).description("Path to the photo resource on the backend server").optional(),
        fieldWithPath("linkedin").type(JsonFieldType.STRING).description("Handle/link to the owner's LinkedIn profile").optional(),
        fieldWithPath("github").type(JsonFieldType.STRING).description("Handle/link to the owner's LinkedIn profile").optional(),
        fieldWithPath("websites[]").type(JsonFieldType.ARRAY).description("Array with relevant websites about the owner").optional(),
        fieldWithPath("websites[].url").type(JsonFieldType.STRING).description("URL to the website").optional(),
        fieldWithPath("websites[].iconPath").type(JsonFieldType.STRING).description("URL to the website's icon").optional(),
        fieldWithPath("roles[]").type(JsonFieldType.ARRAY).description("Array with the roles of the account").optional(),
    )
    private val accountPayloadSchema = PayloadSchema("account", accountFields)
    private val requestOnlyAccountFields = listOf<FieldDescriptor>(
        fieldWithPath("password").type(JsonFieldType.STRING).description("Account password"),
    )
    private val responseOnlyAccountFields = listOf<FieldDescriptor>(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Account ID"),
        fieldWithPath("websites[].id").type(JsonFieldType.NUMBER).description("Related website ID").optional(),
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
                emptyList(),
            ),
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
                    content().json(objectMapper.writeValueAsString(testAccounts)),
                )
                .andDo(
                    document(
                        "account/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Get all the accounts")
                                    .description(
                                        """
                                        The operation returns an array of accounts, allowing to easily retrieve all the created accounts.
                                        """.trimIndent(),
                                    )
                                    .responseSchema(accountPayloadSchema.Response().arraySchema())
                                    .responseFields(accountPayloadSchema.Response().arrayDocumentedFields(responseOnlyAccountFields))
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
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

        @Test
        fun `should return the account`() {
            mockMvc.perform(get("/accounts/{id}", testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(testAccount.name),
                    jsonPath("$.email").value(testAccount.email),
                    jsonPath("$.bio").value(testAccount.bio),
                    jsonPath("$.birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.photoPath").value(testAccount.photoPath),
                    jsonPath("$.linkedin").value(testAccount.linkedin),
                    jsonPath("$.github").value(testAccount.github),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.websites[0].iconPath").value(testAccount.websites[0].iconPath),
                )
                .andDo(
                    document(
                        "accounts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Get accounts by ID")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of a single account using its ID.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the account to retrieve"))
                                    .responseSchema(accountPayloadSchema.Response().schema())
                                    .responseFields(accountPayloadSchema.Response().documentedFields(responseOnlyAccountFields))
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the account does not exist`() {
            mockMvc.perform(get("/accounts/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234"),
                )
                .andDo(
                    document(
                        "accounts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .pathParameters(ResourceDocumentation.parameterWithName("id").description("ID of the account to retrieve"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
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
            mockMvc.perform(
                post("/accounts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testAccount.toJson()),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(testAccount.name),
                    jsonPath("$.email").value(testAccount.email),
                    jsonPath("$.bio").value(testAccount.bio),
                    jsonPath("$.birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.photoPath").value(testAccount.photoPath),
                    jsonPath("$.linkedin").value(testAccount.linkedin),
                    jsonPath("$.github").value(testAccount.github),
                    jsonPath("$.websites.length()").value(1),
                    jsonPath("$.websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.websites[0].iconPath").value(testAccount.websites[0].iconPath),

                )
                .andDo(
                    document(
                        "accounts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Create new accounts")
                                    .description(
                                        """
                                        This endpoint operation creates new accounts. 
                                        """.trimIndent(),
                                    )
                                    .requestSchema(accountPayloadSchema.Request().schema())
                                    .requestFields(accountPayloadSchema.Request().documentedFields(requestOnlyAccountFields))
                                    .responseSchema(accountPayloadSchema.Response().schema())
                                    .responseFields(accountPayloadSchema.Response().documentedFields(responseOnlyAccountFields))
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
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
                "https://github.com",
            )

            mockMvc.perform(
                post("/accounts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "name" to noWebsite.name,
                                "email" to noWebsite.email,
                                "password" to noWebsite.password,
                                "bio" to noWebsite.bio,
                                "birthDate" to noWebsite.birthDate,
                                "photoPath" to noWebsite.photoPath,
                                "linkedin" to noWebsite.linkedin,
                                "github" to noWebsite.github,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.name").value(noWebsite.name),
                    jsonPath("$.email").value(noWebsite.email),
                    jsonPath("$.bio").value(noWebsite.bio),
                    jsonPath("$.birthDate").value(noWebsite.birthDate.toJson()),
                    jsonPath("$.photoPath").value(noWebsite.photoPath),
                    jsonPath("$.linkedin").value(noWebsite.linkedin),
                    jsonPath("$.github").value(noWebsite.github),
                    jsonPath("$.websites.length()").value(0),
                )
                .andDo(
                    document(
                        "accounts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .requestSchema(accountPayloadSchema.Request().schema())
                                    .requestFields(accountPayloadSchema.Request().documentedFields(requestOnlyAccountFields))
                                    .responseSchema(accountPayloadSchema.Response().schema())
                                    .responseFields(accountPayloadSchema.Response().documentedFields(responseOnlyAccountFields))
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/accounts/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            document(
                                "accounts/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    resource(
                                        builder()
                                            .requestSchema(accountPayloadSchema.Request().schema())
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Accounts")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "name" to testAccount.name,
                    "email" to testAccount.email,
                    "password" to testAccount.password,
                    "websites" to emptyList<CustomWebsite>(),
                ),
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
            @DisplayName("photoPath")
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
                        mockMvc.perform(
                            post("/accounts/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                    objectMapper.writeValueAsString(
                                        mapOf(
                                            "name" to testAccount.name,
                                            "email" to testAccount.email,
                                            "password" to testAccount.password,
                                            "websites" to listOf<Any>(params),
                                        ),
                                    ),
                                ),
                        )
                            .andDo(
                                document(
                                    "accounts/{ClassName}/{methodName}",
                                    snippets = arrayOf(
                                        resource(
                                            builder()
                                                .requestSchema(accountPayloadSchema.Request().schema())
                                                .responseSchema(ErrorSchema().Response().schema())
                                                .responseFields(ErrorSchema().Response().documentedFields())
                                                .tag("Accounts")
                                                .build(),
                                        ),
                                    ),
                                ),
                            )
                    },
                    requiredFields = mapOf(
                        "url" to "https://www.google.com",
                    ),
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
            println("testAccount: ${objectMapper.writeValueAsString(testAccount)}")
            mockMvc.post("/accounts/new") {
                contentType = MediaType.APPLICATION_JSON
                content = testAccount.toJson()
            }.andExpect { status { isOk() } }

            mockMvc.perform(
                post("/accounts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testAccount.toJson()),
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("email already exists"),
                )
                .andDo(
                    document(
                        "accounts/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .requestSchema(accountPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Accounts")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
                    "password" to this?.password,
                ),
            ),
        )
    }
}
