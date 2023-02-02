package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import java.util.Calendar
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

@ControllerTest
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: EventRepository
) {
    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        "https://docs.google.com/forms",
        DateInterval(
            TestUtils.createDate(2022, Calendar.JULY, 28),
            TestUtils.createDate(2022, Calendar.JULY, 30)
        ),
        "FEUP",
        "Great Events",
        "https://example.com/exampleThumbnail"
    )

    @NestedTest
    @DisplayName("GET /events")
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                null,
                DateInterval(
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                    null
                ),
                null,
                null,
                "https://example.com/exampleThumbnail2"
            )
        )

        @BeforeEach
        fun addEvents() {
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events`() {
            mockMvc.get("/events")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { json(objectMapper.writeValueAsString(testEvents)) }
                }
        }
    }

    @NestedTest
    @DisplayName("GET /events/{id}")
    inner class GetEvent {
        @BeforeEach
        fun addEvent() {
            repository.save(testEvent)
        }

        @Test
        fun `should return the event`() {
            mockMvc.get("/events/${testEvent.id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testEvent.title) }
                    jsonPath("$.description") { value(testEvent.description) }
                    jsonPath("$.registerUrl") { value(testEvent.registerUrl) }
                    jsonPath("$.dateInterval.startDate") { value(testEvent.dateInterval.startDate.toJson()) }
                    jsonPath("$.dateInterval.endDate") { value(testEvent.dateInterval.endDate.toJson()) }
                    jsonPath("$.location") { value(testEvent.location) }
                    jsonPath("$.category") { value(testEvent.category) }
                    jsonPath("$.thumbnailPath") { value(testEvent.thumbnailPath) }
                }
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.get("/events/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("event not found with id 1234") }
            }
        }
    }

    @NestedTest
    @DisplayName("GET events/category/{category}")
    inner class GetEventsByCategory {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                null,
                DateInterval(
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                    null
                ),
                null,
                null,
                "https://example.com/exampleThumbnail2"
            ),
            Event(
                "Mid event",
                "This event was ok",
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.JANUARY, 15),
                    null
                ),
                null,
                "Other category",
                "https://example.com/exampleThumbnail2"
            ),
            Event(
                "Cool event",
                "This event was a awesome",
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.SEPTEMBER, 11),
                    null
                ),
                null,
                "Great Events",
                "https://example.com/exampleThumbnail2"
            )
        )

        @BeforeEach
        fun addEvents() {
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events of the category`() {
            mockMvc.get("/events/category/${testEvent.category}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].category") { value(testEvent.category) }
                    jsonPath("$[1].category") { value(testEvent.category) }
                }
        }
    }

    @NestedTest
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @Test
        fun `should create a new event`() {
            mockMvc.post("/events/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testEvent)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testEvent.title) }
                    jsonPath("$.description") { value(testEvent.description) }
                    jsonPath("$.registerUrl") { value(testEvent.registerUrl) }
                    jsonPath("$.dateInterval.startDate") { value(testEvent.dateInterval.startDate.toJson()) }
                    jsonPath("$.dateInterval.endDate") { value(testEvent.dateInterval.endDate.toJson()) }
                    jsonPath("$.location") { value(testEvent.location) }
                    jsonPath("$.category") { value(testEvent.category) }
                    jsonPath("$.thumbnailPath") { value(testEvent.thumbnailPath) }
                }
        }

        @NestedTest
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
                    "dateInterval" to testEvent.dateInterval,
                    "thumbnailPath" to testEvent.thumbnailPath
                )
            )

            @NestedTest
            @DisplayName("title")
            inner class TitleValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "title"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${ActivityConstants.Title.minSize} and ${ActivityConstants.Title.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
            }

            @NestedTest
            @DisplayName("description")
            inner class DescriptionValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "description"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${ActivityConstants.Description.minSize} and ${ActivityConstants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Description.minSize,
                        ActivityConstants.Description.maxSize
                    )
            }

            @NestedTest
            @DisplayName("registerUrl")
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

            @NestedTest
            @DisplayName("dateInterval")
            inner class DateIntervalValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "dateInterval"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a DateInterval`() = validationTester.isValidDateInterval()
            }

            @NestedTest
            @DisplayName("location")
            inner class LocationValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "location"
                }

                @Test
                @DisplayName("size should be between ${Constants.Location.minSize} and ${Constants.Location.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Location.minSize, Constants.Location.maxSize)
            }

            @NestedTest
            @DisplayName("category")
            inner class CategoryValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "category"
                }

                @Test
                @DisplayName("size should be between ${Constants.Category.minSize} and ${Constants.Category.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Category.minSize, Constants.Category.maxSize)
            }

            @NestedTest
            @DisplayName("thumbnailPath")
            inner class ThumbnailPathValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "thumbnailPath"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a URL`() = validationTester.isUrl()

                @Test
                fun `should not be empty`() = validationTester.isNotEmpty()
            }
        }
    }

    @NestedTest
    @DisplayName("DELETE /events/{eventId}")
    inner class DeleteEvent {
        @BeforeEach
        fun addEvent() {
            repository.save(testEvent)
        }

        @Test
        fun `should delete the event`() {
            mockMvc.delete("/events/${testEvent.id}").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$") { isEmpty() }
            }

            assert(repository.findById(testEvent.id!!).isEmpty)
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.delete("/events/1234").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("event not found with id 1234") }
            }
        }
    }

    @NestedTest
    @DisplayName("PUT /events/{eventId}")
    inner class UpdateEvent {
        @BeforeEach
        fun addEvent() {
            repository.save(testEvent)
        }

        @Test
        fun `should update the event`() {
            val newTitle = "New event title"
            val newDescription = "New event description"
            val newRegisterUrl = "https://example.com/newUrl"
            val newDateInterval = DateInterval(
                TestUtils.createDate(2022, Calendar.DECEMBER, 1),
                TestUtils.createDate(2022, Calendar.DECEMBER, 2)
            )
            val newLocation = "FLUP"
            val newCategory = "Greatest Events"
            val newThumbnailPath = "https://thumbnails/new.png"

            mockMvc.put("/events/${testEvent.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "description" to newDescription,
                        "registerUrl" to newRegisterUrl,
                        "dateInterval" to newDateInterval,
                        "location" to newLocation,
                        "category" to newCategory,
                        "thumbnailPath" to newThumbnailPath
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(newTitle) }
                    jsonPath("$.description") { value(newDescription) }
                    jsonPath("$.registerUrl") { value(newRegisterUrl) }
                    jsonPath("$.dateInterval.startDate") { value(newDateInterval.startDate.toJson()) }
                    jsonPath("$.dateInterval.endDate") { value(newDateInterval.endDate.toJson()) }
                    jsonPath("$.location") { value(newLocation) }
                    jsonPath("$.category") { value(newCategory) }
                    jsonPath("$.thumbnailPath") { value(newThumbnailPath) }
                }

            val updatedEvent = repository.findById(testEvent.id!!).get()
            assertEquals(newTitle, updatedEvent.title)
            assertEquals(newDescription, updatedEvent.description)
            assertEquals(newRegisterUrl, updatedEvent.registerUrl)
            assertEquals(newDateInterval.startDate.toJson(), updatedEvent.dateInterval.startDate.toJson())
            assertEquals(newDateInterval.endDate.toJson(), updatedEvent.dateInterval.endDate.toJson())
            assertEquals(newLocation, updatedEvent.location)
            assertEquals(newCategory, updatedEvent.category)
            assertEquals(newThumbnailPath, updatedEvent.thumbnailPath)
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.put("/events/1234") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to "New Title",
                        "description" to "New Description",
                        "dateInterval" to DateInterval(TestUtils.createDate(2022, Calendar.DECEMBER, 1), null),
                        "thumbnailPath" to "http://test.com/thumbnail/1"
                    )
                )
            }
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("event not found with id 1234") }
                }
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.put("/events/${testEvent.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(params)
                    }
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "thumbnailPath" to testEvent.thumbnailPath
                )
            )

            @NestedTest
            @DisplayName("title")
            inner class TitleValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "title"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${ActivityConstants.Title.minSize} and ${ActivityConstants.Title.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
            }

            @NestedTest
            @DisplayName("description")
            inner class DescriptionValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "description"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                @DisplayName("size should be between ${ActivityConstants.Description.minSize} and ${ActivityConstants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Description.minSize,
                        ActivityConstants.Description.maxSize
                    )
            }

            @NestedTest
            @DisplayName("registerUrl")
            inner class UrlValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "registerUrl"
                }

                @Test
                fun `should be null or not blank`() = validationTester.isNullOrNotBlank()

                @Test
                fun `should be a URL`() = validationTester.isUrl()
            }

            @NestedTest
            @DisplayName("dateInterval")
            inner class DateIntervalValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "dateInterval"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a DateInterval`() = validationTester.isValidDateInterval()
            }

            @NestedTest
            @DisplayName("location")
            inner class LocationValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "location"
                }

                @Test
                @DisplayName("size should be between ${Constants.Location.minSize} and ${Constants.Location.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Location.minSize, Constants.Location.maxSize)
            }

            @NestedTest
            @DisplayName("category")
            inner class CategoryValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "category"
                }

                @Test
                @DisplayName("size should be between ${Constants.Category.minSize} and ${Constants.Category.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Category.minSize, Constants.Category.maxSize)
            }

            @NestedTest
            @DisplayName("thumbnailPath")
            inner class ThumbnailPathValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "thumbnailPath"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a URL`() = validationTester.isUrl()
            }
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
