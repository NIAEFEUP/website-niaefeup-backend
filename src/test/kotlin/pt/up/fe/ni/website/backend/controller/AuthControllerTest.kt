package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import java.util.Calendar

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
        )
    )

    @Nested
    @DisplayName("POST /auth/new")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetNewToken {
        @BeforeAll
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should fail when email is invalid`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "email" to "president@niaefeup.pt",
                        "password" to testPassword
                    )
                )
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.errors[0].message") { value("account not found with email president@niaefeup.pt") }
            }
        }

        @Test
        fun `should fail when password is incorrect`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, "wrong_password"))
            }.andExpect {
                status { isUnprocessableEntity() }
                jsonPath("$.errors[0].message") { value("invalid credentials") }
            }
        }

        @Test
        fun `should return access and refresh tokens`() {
            mockMvc.post("/auth/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(LoginDto(testAccount.email, testPassword))
            }.andExpect {
                status { isOk() }
                jsonPath("$.access_token") { exists() }
                jsonPath("$.refresh_token") { exists() }
            }
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RefreshToken {
        @BeforeAll
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

    @Nested
    @DisplayName("GET /auth/check")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CheckToken {
        @BeforeAll
        fun setup() {
            repository.save(testAccount)
        }

        @Test
        fun `should fail when no access token is provided`() {
            mockMvc.get("/auth").andExpect {
                status { isUnauthorized() }
                jsonPath("$.errors[0].message") { value("Access is denied") }
            }
        }

        @Test
        fun `should fail when access token is invalid`() {
            mockMvc.get("/auth") {
                header("Authorization", "Bearer invalid_access_token")
            }.andExpect {
                status { isUnauthorized() }
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

        // TODO: Add tests for role access when implemented
    }
}
