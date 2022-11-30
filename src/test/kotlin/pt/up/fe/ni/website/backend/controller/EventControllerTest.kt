package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.MockMvcBase
import pt.up.fe.ni.website.backend.utils.TestUtils
import java.util.*
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

internal class EventControllerTest @Autowired constructor(
        val objectMapper: ObjectMapper,
        val repository: EventRepository
): MockMvcBase() {
    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        TestUtils.createDate(2022, Calendar.JULY, 28)
    )

    @Nested
    @DisplayName("GET /events")
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                TestUtils.createDate(2021, Calendar.OCTOBER, 27)
            )
        )

        @BeforeEach
        fun addEvents() {
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun shouldReturnAllEvents() {
            mockMvc.perform(get("/events"))
                .andExpect {
                    status().isOk
                    content().contentType(MediaType.APPLICATION_JSON)
                    content().json(objectMapper.writeValueAsString(testEvents))
                }
                    .andDo(MockMvcRestDocumentation.document("list-events"))
        }
    }

    @Nested
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @Test
        fun shouldCreateNewEvent() {
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
                fun shouldBeRequired() = validationTester.isRequired()

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
                fun shouldBeRequired() = validationTester.isRequired()

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
                fun shouldBeRequired() = validationTester.isRequired()

                @Test
                fun shouldBeDate() = validationTester.isDate()
            }
        }
    }
}
