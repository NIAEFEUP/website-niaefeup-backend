package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceDocumentation.headerWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.constants.AccountConstants
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAccount
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthCheck
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthNew
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthRefresh
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadRecoverPassword
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentCustomRequestSchema
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentCustomRequestSchemaErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema

@ControllerTest
class AuthControllerTest @Autowired constructor(
    val repository: AccountRepository,
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    passwordEncoder: PasswordEncoder
) {
    final val testPassword = "testPassword"

    // TODO: Make sure to add "MEMBER" role to the account
    val testAccount = Account(
        "Test Account",
        "test_account@test.com",
        passwordEncoder.encode(testPassword),
        "This is a test account",
        TestUtils.createDate(2001, Calendar.JULY, 28),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        ),
        mutableListOf()
    )

    private val checkAuthHeaders = listOf<HeaderDescriptorWithType>(
        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer authentication token")
    )

    @NestedTest
    @DisplayName("POST /auth/new")
    inner class GetNewToken {
        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        val documentation: ModelDocumentation = PayloadAuthNew()

        @Test
        fun `should fail when email is not registered`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "email" to "president@niaefeup.pt",
                                "password" to testPassword
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isNotFound,
                    jsonPath("$.errors[0].message").value("account not found with email president@niaefeup.pt")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail when password is incorrect`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginDto(testAccount.email, "wrong_password")))
            )
                .andExpectAll(
                    status().isUnauthorized,
                    jsonPath("$.errors[0].message").value("invalid credentials")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should return access and refresh tokens`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword)))
            )
                .andExpectAll(
                    status().isOk,
                    jsonPath("$.access_token").exists(),
                    jsonPath("$.refresh_token").exists()
                )
                .andDocument(
                    documentation,
                    "Authenticate account",
                    "This endpoint operation allows authentication using user's password and email, " +
                        "generating new access and refresh tokens to be used in following requests.",
                    documentRequestPayload = true
                )
        }
    }

    @NestedTest
    @DisplayName("POST /auth/refresh")
    inner class RefreshToken {
        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        val documentation: ModelDocumentation = PayloadAuthRefresh()

        @Test
        fun `should fail when refresh token is invalid`() {
            mockMvc.perform(
                post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(TokenDto("invalid_refresh_token")))
            )
                .andExpectAll(
                    status().isUnauthorized,
                    jsonPath("$.errors[0].message").value("invalid refresh token")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should return new access token`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andReturn().response.let { response ->
                val refreshToken = objectMapper.readTree(response.contentAsString)["refresh_token"].asText()
                mockMvc.perform(
                    post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TokenDto(refreshToken)))
                )
                    .andExpectAll(
                        status().isOk,
                        jsonPath("$.access_token").exists()
                    )
                    .andDocument(
                        documentation,
                        "Refresh access token",
                        "This endpoint operation allows the renewal of the access token, " +
                            "using the currently valid refresh token.",
                        documentRequestPayload = true
                    )
            }
        }
    }

    @NestedTest
    @DisplayName("POST /auth/password/recovery")
    inner class RecoverPasswordRequest {
        var documentation: ModelDocumentation = PayloadRecoverPassword()

        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should fail if email is not found`() {
            mockMvc.perform(
                post("/auth/password/recovery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("email" to "dont@exist.com")))
            )
                .andExpectAll(
                    status().isNotFound(),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with email dont@exist.com")
                )
                .andDocumentErrorResponse(
                    documentation,
                    "Recover password",
                    "This endpoint operation allows the recovery of the password of an account, " +
                        "sending an email with a link to reset the password.",
                    documentRequestPayload = true
                )
        }

        @Test
        fun `should return password recovery link`() {
            mockMvc.perform(
                post("/auth/password/recovery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("email" to testAccount.email)))
            )
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.recovery_url").exists()
                ).andDocument(
                    documentation,
                    "Recover password",
                    "This endpoint operation allows the recovery of the password of an account, " +
                        "sending an email with a link to reset the password.",
                    documentRequestPayload = true
                )
        }
    }

    @NestedTest
    @DisplayName("POST /auth/password/recovery/{token}/confirm")
    inner class RecoverPasswordConfirm {
        @field:Value("\${page.recover-password}")
        private lateinit var recoverPasswordPage: String

        private val newPassword = "new-password"

        private val parameters = listOf(
            ResourceDocumentation.parameterWithName("token").description("The recovery token sent to the user's email.")
        )

        private val passwordRecoveryPayload = PayloadSchema(
            "password-recover",
            mutableListOf(
                DocumentedJSONField("password", "The new password.", JsonFieldType.STRING)
            )
        )

        private val documentation: ModelDocumentation = PayloadAccount()

        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should update the password`() {
            mockMvc.perform(
                post("/auth/password/recovery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "email" to testAccount.email
                            )
                        )
                    )
            ).andReturn().response.let { authResponse ->
                val token = objectMapper.readTree(authResponse.contentAsString)["recovery_url"].asText()
                    .removePrefix("$recoverPasswordPage/")
                    .removeSuffix("/confirm")
                mockMvc.perform(
                    post("/auth/password/recovery/{token}/confirm", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "password" to newPassword
                                )
                            )
                        )
                ).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(testAccount.id)
                ).andDocumentCustomRequestSchema(
                    documentation,
                    passwordRecoveryPayload,
                    "Recover password",
                    "Update the password of an account using a recovery token.",
                    urlParameters = parameters,
                    documentRequestPayload = true
                )
            }

            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "email" to testAccount.email,
                        "password" to newPassword
                    )
                )
            }.andExpect { status { isOk() } }
        }

        @Test
        fun `should fail when token is invalid`() {
            mockMvc.perform(
                post("/auth/password/recovery/{token}/confirm", "invalid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "password" to newPassword
                            )
                        )
                    )
            ).andExpectAll(
                status().isUnauthorized(),
                jsonPath("$.errors.length()").value(1),
                jsonPath("$.errors[0].message").value("invalid password recovery token")
            ).andDocumentCustomRequestSchemaErrorResponse(
                documentation,
                passwordRecoveryPayload,
                "Recover password",
                "Update the password of an account using a recovery token.",
                urlParameters = parameters,
                documentRequestPayload = true
            )

            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "email" to testAccount.email,
                        "password" to newPassword
                    )
                )
            }.andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `should fail when using recovery token twice`() {
            mockMvc.perform(
                post("/auth/password/recovery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "email" to testAccount.email
                            )
                        )
                    )
            )
                .andReturn().response.let { authResponse ->
                    val token = objectMapper.readTree(authResponse.contentAsString)["recovery_url"].asText()
                        .removePrefix("$recoverPasswordPage/")
                        .removeSuffix("/confirm")
                    mockMvc.perform(
                        post("/auth/password/recovery/{token}/confirm", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    mapOf(
                                        "password" to newPassword
                                    )
                                )
                            )
                    ).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id").value(testAccount.id)
                    )
                    mockMvc.perform(
                        post("/auth/password/recovery/{token}/confirm", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    mapOf(
                                        "password" to "not using password"
                                    )
                                )
                            )
                    ).andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors[0].message").value("invalid password recovery token")
                    ).andDocumentCustomRequestSchemaErrorResponse(
                        documentation,
                        passwordRecoveryPayload,
                        "Recover password",
                        "Update the password of an account using a recovery token.",
                        urlParameters = parameters,
                        documentRequestPayload = true
                    )
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/auth/password/recovery/random-token/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params))
                    )
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "password" to "new-password"
                )
            )

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
                @DisplayName(
                    "size should be between ${AccountConstants.Password.minSize}" +
                        " and ${AccountConstants.Password.maxSize}()"
                )
                fun size() = validationTester.hasSizeBetween(
                    AccountConstants.Password.minSize,
                    AccountConstants.Password.maxSize
                )
            }
        }
    }

    @NestedTest
    @DisplayName("GET /auth")
    inner class CheckToken {
        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        val documentation: ModelDocumentation = PayloadAuthCheck()

        @Test
        fun `should fail when no access token is provided`() {
            mockMvc.perform(get("/auth")).andExpectAll(
                status().isForbidden,
                jsonPath("$.errors[0].message").value("Access Denied")
            )
                .andDocumentErrorResponse(documentation)
        }

        @Test
        fun `should fail when access token is invalid`() {
            mockMvc.perform(
                get("/auth")
                    .header("Authorization", "Bearer invalid_access_token")
            )
                .andExpectAll(
                    status().isUnauthorized,
                    jsonPath("$.errors[0].message").value(
                        startsWith("An error occurred while attempting to decode the Jwt")
                    )
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should return authenticated user`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andReturn().response.let { response ->
                val accessToken = objectMapper.readTree(response.contentAsString)["access_token"].asText()

                mockMvc.perform(
                    get("/auth")
                        .header("Authorization", "Bearer $accessToken")
                ).andExpectAll(
                    status().isOk,
                    jsonPath("$.authenticated_user.email").value(testAccount.email)
                )
                    .andDocument(
                        documentation,
                        "Check access token",
                        "This endpoint operation allows to check if a given access token is valid, returning " +
                            "the associated account's information.",
                        checkAuthHeaders,
                        documentRequestPayload = true
                    )
            }
        }
    }
}
