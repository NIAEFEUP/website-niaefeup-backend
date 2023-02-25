package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation.headerWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
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
import pt.up.fe.ni.website.backend.utils.documentation.DocumentationHelper.Companion.addFieldsToPayloadBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema

@ControllerTest
@AutoConfigureRestDocs
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
        emptyList()
    )

    private val requestOnlyNewAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("email").type(JsonFieldType.STRING).description("Email of the account"),
        fieldWithPath("password").type(JsonFieldType.STRING).description("Password of the account")
    )

    private val responseOnlyNewAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("access_token").type(JsonFieldType.STRING).description("Access token, used to identify the user"),
        fieldWithPath("refresh_token").type(JsonFieldType.STRING).description(
            "Refresh token, used to refresh the access token"
        )
    )
    private val newAuthPayloadSchema = PayloadSchema("auth-new", emptyList())

    private val requestOnlyRefreshAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("token").type(JsonFieldType.STRING).description("Refresh token, used to refresh the access token")
    )
    private val responseOnlyRefreshAuthFields = listOf<FieldDescriptor>(
        fieldWithPath("access_token").type(JsonFieldType.STRING).description("Access token, used to identify the user")
    )
    private val refreshAuthPayloadSchema = PayloadSchema("auth-refresh", emptyList())

    private val checkAuthHeaders = listOf<HeaderDescriptorWithType>(
        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer authentication token")
    )
    private val checkAuthPayloadSchema = PayloadSchema(
        "auth-check",
        listOf<FieldDescriptor>(
            fieldWithPath("authenticated_user").type(JsonFieldType.OBJECT).description(
                "Authenticated account's information."
            )
        )
    )

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
                                "password" to testPassword
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isNotFound,
                    jsonPath("$.errors[0].message").value("account not found with email president@niaefeup.pt")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .requestSchema(newAuthPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build()
                            )
                        )
                    )
                )
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
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .requestSchema(newAuthPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build()
                            )
                        )
                    )
                )
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
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Authenticate account")
                                    .description(
                                        """
                                        This endpoint operation allows authentication using user's password and email, generating new access and refresh tokens to be used in later communication.
                                        """.trimIndent()
                                    )
                                    .requestSchema(newAuthPayloadSchema.Request().schema())
                                    .requestFields(
                                        newAuthPayloadSchema.Request().documentedFields(requestOnlyNewAuthFields)
                                    )
                                    .responseSchema(newAuthPayloadSchema.Response().schema())
                                    .responseFields(
                                        newAuthPayloadSchema.Response().documentedFields(responseOnlyNewAuthFields)
                                    )
                                    .tag("Authentication")
                                    .build()
                            )
                        )
                    )
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
            mockMvc.perform(
                post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(TokenDto("invalid_refresh_token")))
            )
                .andExpectAll(
                    status().isUnauthorized,
                    jsonPath("$.errors[0].message").value("invalid refresh token")
                )
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .requestSchema(refreshAuthPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build()
                            )
                        )
                    )
                )
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
                    .andDo(
                        MockMvcRestDocumentationWrapper.document(
                            "auth/{ClassName}/{methodName}",
                            snippets = arrayOf(
                                resource(
                                    builder()
                                        .summary("Refresh access token")
                                        .description(
                                            """
                                        This endpoint operation allows the renewal of access tokens when expiring, using the appropriate refresh token.
                                            """.trimIndent()
                                        )
                                        .requestSchema(refreshAuthPayloadSchema.Request().schema())
                                        .requestFields(
                                            refreshAuthPayloadSchema.Request().documentedFields(
                                                requestOnlyRefreshAuthFields
                                            )
                                        )
                                        .responseSchema(refreshAuthPayloadSchema.Response().schema())
                                        .responseFields(
                                            refreshAuthPayloadSchema.Response().documentedFields(
                                                responseOnlyRefreshAuthFields
                                            )
                                        )
                                        .tag("Authentication")
                                        .build()
                                )
                            )
                        )
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

        @Test
        fun `should fail when no access token is provided`() {
            mockMvc.perform(get("/auth")).andExpectAll(
                status().isForbidden,
                jsonPath("$.errors[0].message").value("Access Denied")
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
                                    .build()
                            )
                        )
                    )
                )
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
                .andDo(
                    MockMvcRestDocumentationWrapper.document(
                        "auth/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Authentication")
                                    .build()
                            )
                        )
                    )
                )
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
                    .andDo(
                        MockMvcRestDocumentationWrapper.document(
                            "auth/{ClassName}/{methodName}",
                            snippets = arrayOf(
                                resource(
                                    builder()
                                        .summary("Check access token")
                                        .description(
                                            """
                                        This endpoint operation allows to check if a given access token is valid, returning the associated account's information.
                                            """.trimIndent()
                                        )
                                        .requestHeaders(checkAuthHeaders)
                                        .responseSchema(checkAuthPayloadSchema.Response().schema())
                                        .responseFields(
                                            checkAuthPayloadSchema.Response().documentedFields()
                                                .addFieldsToPayloadBeneathPath(
                                                    "authenticated_user",
                                                    AccountControllerTest.accountPayloadSchema.Response()
                                                        .documentedFields(
                                                            AccountControllerTest.responseOnlyAccountFields
                                                        )
                                                )
                                        )
                                        .tag("Authentication")
                                        .build()
                                )
                            )
                        )
                    )
            }
        }
    }
}
