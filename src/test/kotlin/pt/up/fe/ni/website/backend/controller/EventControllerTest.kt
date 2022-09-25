package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import pt.up.fe.ni.website.backend.model.Event
import java.util.Calendar
import java.util.TimeZone

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {
    @Nested
    @DisplayName("GET /events")
    inner class GetAllEvents {
        @BeforeEach
        fun addEvents() {
            val testEvents = listOf(
                Event(
                    "Great event",
                    "This was a nice and iconic event",
                    Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        .apply { set(2022, Calendar.JULY, 28, 0, 0, 0) }
                        .time
                ),
                Event(
                    "Bad event",
                    "This event was a failure",
                    Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        .apply { set(2021, Calendar.OCTOBER, 27, 0, 0, 0) }
                        .time
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
                    jsonPath("$[0].title") { value("Great event") }
                    jsonPath("$[0].description") { value("This was a nice and iconic event") }
                    jsonPath("$[0].date") { containsString("2022-07-28T") }
                }
        }
    }
}
