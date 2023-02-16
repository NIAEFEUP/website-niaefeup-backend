package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema
import java.util.Calendar

@ControllerTest
@AutoConfigureRestDocs
class AuthControllerTest @Autowired constructor(
    val repository: AccountRepository,
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    passwordEncoder: PasswordEncoder,
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png"),
        ),
        emptyList(),
    )

    private val requestOnlyAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("email").type(JsonFieldType.STRING).description("Email of the account"),
        fieldWithPath("password").type(JsonFieldType.STRING).description("Password of the account"),
    )

    private val responseOnlyAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("access_token").type(JsonFieldType.STRING).description("Access token, used to identify the user"),
        fieldWithPath("refresh_token").type(JsonFieldType.STRING).description("Refresh token, used to renew the session"),

    )
    private val authPayloadSchema = PayloadSchema("auth", emptyList())

    @NestedTest
    @DisplayName("POST /auth/new")
    inner class GetNewToken {
        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should fail when email is not registered`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "email" to "president@niaefeup.pt",
                                "password" to testPassword,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isNotFound,
                    jsonPath("$.errors[0].message").value("account not found with email president@niaefeup.pt"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail when password is incorrect`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginDto(testAccount.email, "wrong_password"))),
            )
                .andExpectAll(
                    status().isUnauthorized,
                    jsonPath("$.errors[0].message").value("invalid credentials"),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should return access and refresh tokens`() {
            mockMvc.perform(
                post("/auth/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))),
            )
                .andExpectAll(
                    status().isOk,
                    jsonPath("$.access_token").exists(),
                    jsonPath("$.refresh_token").exists(),
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Authenticate account")
                                    .description(
                                        """
                                        This endpoint operation allows users to authenticate using their password and email, generating new access and refresh tokens to be used in later communication.
                                        """.trimIndent(),
                                    )
                                    .requestSchema(authPayloadSchema.Request().schema())
                                    .requestFields(authPayloadSchema.Request().documentedFields(requestOnlyAuthFields))
                                    .responseSchema(authPayloadSchema.Response().schema())
                                    .responseFields(authPayloadSchema.Response().documentedFields(responseOnlyAuthFields))
                                    .tag("Authentication")
                                    .build(),
                            ),
                        ),
                    ),
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

        @Test
        fun `should fail when refresh token is invalid`() {
            mockMvc.post("/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(TokenDto("invalid_refresh_token"))
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.errors[0].message") { value("invalid refresh token") }
            }
        }

        @Test
        fun `should return new access token`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andReturn().response.let { response ->
                val refreshToken = objectMapper.readTree(response.contentAsString)["refresh_token"].asText()
                mockMvc.post("/auth/refresh") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(TokenDto(refreshToken))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.access_token") { exists() }
                }
            }
        }
    }

    @NestedTest
    @DisplayName("GET /auth/check")
    inner class CheckToken {
        @BeforeEach
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should fail when no access token is provided`() {
            mockMvc.get("/auth").andExpect {
                status { isForbidden() }
                jsonPath("$.errors[0].message") { value("Access Denied") }
            }
        }

        @Test
        fun `should fail when access token is invalid`() {
            mockMvc.get("/auth") {
                header("Authorization", "Bearer invalid_access_token")
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.errors[0].message") { startsWith("An error occurred while attempting to decode the Jwt") }
            }
        }

        @Test
        fun `should return authenticated user`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andReturn().response.let { response ->
                val accessToken = objectMapper.readTree(response.contentAsString)["access_token"].asText()
                mockMvc.get("/auth") {
                    header("Authorization", "Bearer $accessToken")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.authenticated_user") { value(testAccount.email) }
                }
            }
        }
    }
}
