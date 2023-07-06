package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ResourceDocumentation.headerWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.dto.auth.LoginDto
import pt.up.fe.ni.website.backend.dto.auth.TokenDto
import pt.up.fe.ni.website.backend.model.*
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.repository.PerActivityRoleRepository
import pt.up.fe.ni.website.backend.repository.RoleRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthCheck
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthNew
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadAuthRefresh
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

@ControllerTest
class AuthControllerTest @Autowired constructor(
    val repository: AccountRepository,
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    final val passwordEncoder: PasswordEncoder,
    val activityRepository: ActivityRepository<Activity>,
    val roleRepository: RoleRepository
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

    @NestedTest
    @DisplayName("POST /auth/hasPermission/")
    inner class CheckPermissions {
        private val testAccountWithRole = Account(
            "Test Account 2",
            "test_account2@test.com",
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
        private val testPermissions = listOf(Permission.CREATE_ACCOUNT, Permission.CREATE_ACTIVITY)
        private val testRole = Role("MEMBER", Permissions(listOf(Permission.CREATE_ACCOUNT, Permission.CREATE_ACTIVITY)), false)
        private val testPerActivityRole = PerActivityRole(Permissions(listOf(Permission.CREATE_ACCOUNT, Permission.CREATE_ACTIVITY)))
        private val activity = Project("Test Activity", "Test Description", mutableListOf(), mutableListOf())


        @BeforeEach
        fun setup() {
            testPerActivityRole.activity = activity
            activityRepository.save(activity)
            testRole.associatedActivities.add(testPerActivityRole)
            testAccountWithRole.roles.add(testRole)

            roleRepository.save(testRole)
            repository.save(testAccountWithRole)
            repository.save(testAccount)
        }

        @Test
        fun `should fail when user has no roles`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andReturn().response.let { response ->
                val accessToken = objectMapper.readTree(response.contentAsString)["access_token"].asText()
                mockMvc.perform(
                    get("/auth/hasPermission/${testPermissions[0].toString().trim().uppercase(Locale.getDefault())}")
                        .header("Authorization", "Bearer $accessToken")
                ).andExpect(status().isForbidden)
            }
        }

        @Test
        fun `should fail when user doesn't have permission`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccountWithRole.email, testPassword))
            }.andReturn().response.let { response ->
                val accessToken = objectMapper.readTree(response.contentAsString)["access_token"].asText()
                mockMvc.perform(
                    get("/auth/hasPermission/${testPermissions[0].toString().trim().uppercase(Locale.getDefault())}")
                        .header("Authorization", "Bearer $accessToken")
                ).andExpect(status().isOk)
            }
        }
    }
}
