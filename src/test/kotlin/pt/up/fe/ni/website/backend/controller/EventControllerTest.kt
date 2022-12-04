package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
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
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.EndpointTest
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
        TestUtils.createDate(2022, Calendar.JULY, 28),
        TestUtils.createDate(2022, Calendar.JULY, 30),
        "https://example.com/",
        "FEUP",
        "Great Events",
        "https://example.com/exampleThumbnail"
    )

    @EndpointTest
    @DisplayName("GET /events")
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                null,
                TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                null,
                null,
                null,
                null,
                "https://example.com/exampleThumbnail2"
            )
        )

        @BeforeAll
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

    @EndpointTest
    @DisplayName("GET /events/{id}")
    inner class GetEvent {
        @BeforeAll
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
                    jsonPath("$.startDate") { value(testEvent.startDate.toJson()) }
                    jsonPath("$.endDate") { value(testEvent.endDate.toJson()) }
                    jsonPath("$.url") { value(testEvent.url) }
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

    @EndpointTest
    @DisplayName("GET events/category/{category}")
    inner class GetEventsByCategory {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                null,
                TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                null,
                null,
                null,
                null,
                "https://example.com/exampleThumbnail2"
            ),
            Event(
                "Mid event",
                "This event was ok",
                null,
                TestUtils.createDate(2022, Calendar.JANUARY, 15),
                null,
                null,
                null,
                "Other category",
                "https://example.com/exampleThumbnail2"
            )
        )

        @BeforeAll
        fun addEvents() {
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events of the category`() {
            mockMvc.get("/events/category/${testEvent.category}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].title") { value(testEvent.title) }
                    jsonPath("$[0].description") { value(testEvent.description) }
                    jsonPath("$[0].startDate") { value(testEvent.startDate.toJson()) }
                    jsonPath("$[0].endDate") { value(testEvent.endDate.toJson()) }
                    jsonPath("$[0].url") { value(testEvent.url) }
                    jsonPath("$[0].location") { value(testEvent.location) }
                    jsonPath("$[0].category") { value(testEvent.category) }
                    jsonPath("$[0].thumbnailPath") { value(testEvent.thumbnailPath) }
                }
        }
    }

    @EndpointTest
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @AfterEach
        fun clearEvents() {
            repository.deleteAll()
        }

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
                    jsonPath("$.startDate") { value(testEvent.startDate.toJson()) }
                    jsonPath("$.endDate") { value(testEvent.endDate.toJson()) }
                    jsonPath("$.url") { value(testEvent.url) }
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
                    "startDate" to testEvent.startDate,
                    "endDate" to testEvent.endDate,
                    "url" to testEvent.url,
                    "location" to testEvent.location,
                    "category" to testEvent.category,
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
                fun size() = validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
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
                    validationTester.hasSizeBetween(ActivityConstants.Description.minSize, ActivityConstants.Description.maxSize)
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
            @DisplayName("startDate")
            inner class StartDateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "startDate"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a Date`() = validationTester.isDate()
            }

            @NestedTest
            @DisplayName("endDate")
            inner class EndDateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "endDate"
                }

                @Test
                fun `should be a Date`() = validationTester.isDate()
            }

            @NestedTest
            @DisplayName("url")
            inner class UrlValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "url"
                }

                @Test
                fun `should be a URL`() = validationTester.isUrl()
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

    @EndpointTest
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

    @EndpointTest
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
            val newStartDate = TestUtils.createDate(2022, Calendar.DECEMBER, 1)
            val newEndDate = TestUtils.createDate(2022, Calendar.DECEMBER, 2)
            val newUrl = "https://example.com/newUrl"
            val newLocation = "FLUP"
            val newCategory = "Greatest Events"
            val newThumbnailPath = "https://thumbnails/new.png"

            mockMvc.put("/events/${testEvent.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to newTitle,
                        "description" to newDescription,
                        "startDate" to newStartDate,
                        "endDate" to newEndDate,
                        "url" to newUrl,
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
                    jsonPath("$.startDate") { value(newStartDate.toJson()) }
                    jsonPath("$.endDate") { value(newEndDate.toJson()) }
                    jsonPath("$.url") { value(newUrl) }
                    jsonPath("$.location") { value(newLocation) }
                    jsonPath("$.category") { value(newCategory) }
                    jsonPath("$.thumbnailPath") { value(newThumbnailPath) }
                }

            val updatedEvent = repository.findById(testEvent.id!!).get()
            assertEquals(newTitle, updatedEvent.title)
            assertEquals(newDescription, updatedEvent.description)
            assertEquals(newStartDate.toJson(), updatedEvent.startDate.toJson())
            assertEquals(newEndDate.toJson(), updatedEvent.endDate.toJson())
            assertEquals(newUrl, updatedEvent.url)
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
                        "startDate" to TestUtils.createDate(2022, Calendar.DECEMBER, 1),
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
                    "startDate" to testEvent.startDate,
                    "endDate" to testEvent.endDate,
                    "url" to testEvent.url,
                    "location" to testEvent.location,
                    "category" to testEvent.category,
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
                fun size() = validationTester.hasSizeBetween(ActivityConstants.Title.minSize, ActivityConstants.Title.maxSize)
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
                    validationTester.hasSizeBetween(ActivityConstants.Description.minSize, ActivityConstants.Description.maxSize)
            }

            @NestedTest
            @DisplayName("startDate")
            inner class StartDateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "startDate"
                }

                @Test
                fun `should be required`() = validationTester.isRequired()

                @Test
                fun `should be a Date`() = validationTester.isDate()
            }

            @NestedTest
            @DisplayName("endDate")
            inner class EndDateValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "endDate"
                }

                @Test
                fun `should be a Date`() = validationTester.isDate()
            }

            @NestedTest
            @DisplayName("url")
            inner class UrlValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "url"
                }

                @Test
                fun `should be a URL`() = validationTester.isUrl()
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
