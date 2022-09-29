package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.utils.TestUtils
import java.util.Calendar
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {
    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        TestUtils.createDate(2022, Calendar.JULY, 28)
    )

    @Nested
    @DisplayName("GET /events")
    inner class GetAllEvents {
        @BeforeEach
        fun addEvents() {
            val testEvents = listOf(
                testEvent,
                Event(
                    "Bad event",
                    "This event was a failure",
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27)
                )
            )

            for (event in testEvents) {
                mockMvc.post("/events/new") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(event)
                }
            }
        }

        @Test
        fun `should return all events`() {
            mockMvc.get("/events")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value(testEvent.title) }
                    jsonPath("$[0].description") { value(testEvent.description) }
                    jsonPath("$[0].date") { value(containsString("2022-07-28T")) }
                }
        }
    }

    @Nested
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @Test
        fun `should create a new event`() {
            mockMvc.post("/events/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testEvent)
            }
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testEvent.title) }
                    jsonPath("$.description") { value(testEvent.description) }
                    jsonPath("$.date") { value(containsString("2022-07-28T")) }
                }
        }

        @Nested
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any> ->
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
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
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
