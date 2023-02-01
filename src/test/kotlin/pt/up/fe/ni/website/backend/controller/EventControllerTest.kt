package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import java.util.Calendar
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: EventRepository
) {
    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        "https://docs.google.com/forms",
        TestUtils.createDate(2022, Calendar.JULY, 28)
    )

    private val eventArrayResponseSchema = Schema("event-array-response")
    private val eventResponseArray = mutableListOf<FieldDescriptor>(
        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("Event ID"),
        fieldWithPath("[].title").type(JsonFieldType.STRING).description("Event title"),
        fieldWithPath("[].description").type(JsonFieldType.STRING).description("Event description"),
        fieldWithPath("[].registerUrl").type(JsonFieldType.STRING).description("Link to the event registration").optional(),
        fieldWithPath("[].date").type(JsonFieldType.STRING).description("Event date")
    )

    private val eventResponseSchema = Schema("event-response")
    private val eventResponse = mutableListOf<FieldDescriptor>(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Event ID"),
        fieldWithPath("title").type(JsonFieldType.STRING).description("Event title"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("Event description"),
        fieldWithPath("registerUrl").type(JsonFieldType.STRING).description("Link to the event registration").optional(),
        fieldWithPath("date").type(JsonFieldType.STRING).description("Event date")
    )

    private val eventRequestSchema = Schema("event-request")
    private val eventRequest = mutableListOf<FieldDescriptor>(
        fieldWithPath("title").type(JsonFieldType.STRING).description("Event title"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("Event description"),
        fieldWithPath("registerUrl").type(JsonFieldType.STRING).description("Link to the event registration").optional(),
        fieldWithPath("date").type(JsonFieldType.STRING).description("Event date")
    )

    @Nested
    @DisplayName("GET /events")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                null,
                TestUtils.createDate(2021, Calendar.OCTOBER, 27)
            )
        )

        @BeforeAll
        fun addEvents() {
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events`() {
            mockMvc.perform(get("/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(testEvents)))
                .andDo(
                    document(
                        "events/{ClassName}",
                        snippets = arrayOf(
                            resource(
                                ResourceSnippetParameters.builder()
                                    .summary("Gets all the events.")
                                    .description(
                                        """
                                        Visiting the events page on the frontend requires all events to be loaded.
                                        """.trimIndent()
                                    )
                                    .responseSchema(eventArrayResponseSchema)
                                    .tag("Events")
                                    .responseFields(eventResponseArray)
                                    .build()
                            )
                        )
                    )
                )
        }
    }

    @Nested
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @Test
        fun `should create a new event`() {
            mockMvc.perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEvent))
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.registerUrl").value(testEvent.registerUrl),
                    jsonPath("$.date").value(containsString("28-07-2022"))
                )
                .andDo(
                    document(
                        "events/{ClassName}",
                        snippets = arrayOf(
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Events")
                                    .summary("Creates a new event.")
                                    .description(
                                        """
                                        It is necessary to create new events, as the nucleus is very active
                                        """.trimIndent()
                                    )
                                    .requestFields(eventRequest)
                                    .requestSchema(eventRequestSchema)
                                    .responseFields(eventResponse)
                                    .responseSchema(eventResponseSchema)
                                    .build()
                            )
                        )
                    )
                )
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.post("/events/new") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "date" to testEvent.date
                )
            )

            @Nested
            @DisplayName("title")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class TitleValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "title"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Title.minSize} and ${Constants.Title.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Title.minSize, Constants.Title.maxSize)
            }

            @Nested
            @DisplayName("description")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class DescriptionValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "description"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${Constants.Description.minSize} and ${Constants.Description.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
            }

            @Nested
            @DisplayName("registerUrl")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class RegisterUrlValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "registerUrl"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be a URL`() = validationTester.isUrl()
            }

            @Nested
            @DisplayName("date")
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            inner class DateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "date"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a Date`() = validationTester.isDate()
            }
        }
    }
}
