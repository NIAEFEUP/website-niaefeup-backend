package pt.up.fe.ni.website.backend.utils

import capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson
import capital.scalable.restdocs.AutoDocumentation.authorization
import capital.scalable.restdocs.AutoDocumentation.description
import capital.scalable.restdocs.AutoDocumentation.methodAndPath
import capital.scalable.restdocs.AutoDocumentation.modelAttribute
import capital.scalable.restdocs.AutoDocumentation.pathParameters
import capital.scalable.restdocs.AutoDocumentation.requestFields
import capital.scalable.restdocs.AutoDocumentation.requestParameters
import capital.scalable.restdocs.AutoDocumentation.responseFields
import capital.scalable.restdocs.AutoDocumentation.section
import capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson
import capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.cli.CliDocumentation.curlRequest
import org.springframework.restdocs.http.HttpDocumentation.httpRequest
import org.springframework.restdocs.http.HttpDocumentation.httpResponse
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.Base64Utils
import org.hamcrest.Matchers.`is`
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import javax.servlet.Filter

/*
* Based on MockMvcBase from spring-auto-rest-docs documentation and example:
* https://github.com/ScaCap/spring-auto-restdocs/blob/master/samples/kotlin-webmvc/src/test/kotlin/capital/scalable/restdocs/example/testsupport/MockMvcBase.kt
*
* */

private const val DEFAULT_AUTHORIZATION = "Resource is public."

@Extensions(ExtendWith(RestDocumentationExtension::class), ExtendWith(SpringExtension::class))
@SpringBootTest
//@AutoConfigureMockMvc
@AutoConfigureTestDatabase
abstract class MockMvcBase {
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Qualifier("requestContextFilter")
    @Autowired
    private lateinit var springSecurityFilterChain: Filter

    @Autowired
    private lateinit var requestMappingHandlerAdapter: RequestMappingHandlerAdapter

    protected lateinit var mockMvc: MockMvc

    @BeforeEach
    @Throws(Exception::class)
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters<DefaultMockMvcBuilder>(springSecurityFilterChain)
                .alwaysDo<DefaultMockMvcBuilder>(prepareJackson(objectMapper))
                .alwaysDo<DefaultMockMvcBuilder>(commonDocumentation())
                .apply<DefaultMockMvcBuilder>(documentationConfiguration(restDocumentation).uris()
                .withScheme("http").withHost("localhost").withPort(8080)
                .and()
                .snippets().withDefaults(curlRequest(), httpRequest(), httpResponse(), requestFields(),
                                responseFields(), pathParameters(), requestParameters(), description(), methodAndPath(),
                                section(), authorization(DEFAULT_AUTHORIZATION),
                                modelAttribute(requestMappingHandlerAdapter.argumentResolvers))).build()
    }

    protected fun commonDocumentation(): RestDocumentationResultHandler {
        return document("{class-name}/{method-name}", preprocessRequest(), commonResponsePreprocessor())
    }

    protected fun commonResponsePreprocessor(): OperationResponsePreprocessor {
        return preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper), prettyPrint())
    }

    protected fun userToken(): RequestPostProcessor {
        return RequestPostProcessor { request ->
            // If the tests requires setup logic for users, you can place it here.
            // Authorization headers or cookies for users should be added here as well.
            val accessToken: String
            try {
                accessToken = getAccessToken("test", "test")
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

            request.addHeader("Authorization", "Bearer $accessToken")
            documentAuthorization(request, "User access token required.")
        }
    }

    @Throws(Exception::class)
    private fun getAccessToken(username: String, password: String): String {
        val authorization = "Basic " + String(Base64Utils.encode("app:very_secret".toByteArray()))
        val contentType = MediaType.APPLICATION_JSON.toString() + ";charset=UTF-8"

        val body = mockMvc.perform(post("/oauth/token").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username).param("password", password)
                .param("grant_type", "password").param("scope", "read write")
                .param("client_id", "app").param("client_secret", "very_secret"))
                    .andExpect(status().isOk).andExpect(content().contentType(contentType))
                    .andExpect(jsonPath("$.access_token", `is`(notNullValue())))
                    .andExpect(jsonPath("$.token_type", `is`(equalTo("bearer"))))
                    .andExpect(jsonPath("$.refresh_token", `is`(notNullValue())))
                    .andExpect(jsonPath("$.expires_in", `is`(greaterThan(4000))))
                    .andExpect(jsonPath("$.scope", `is`(equalTo("read write"))))
                .andReturn().response.contentAsString

        return body.substring(17, 53)
    }
}