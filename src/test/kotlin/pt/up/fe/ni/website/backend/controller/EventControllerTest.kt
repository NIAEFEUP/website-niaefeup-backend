package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.config.upload.UploadConfigProperties
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model.PayloadEvent
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocument
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentEmptyObjectResponse
import pt.up.fe.ni.website.backend.utils.documentation.utils.MockMVCExtension.Companion.andDocumentErrorResponse
import pt.up.fe.ni.website.backend.utils.mockmvc.multipartBuilder

@ControllerTest
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: EventRepository,
    val accountRepository: AccountRepository,
    val uploadConfigProperties: UploadConfigProperties
) {
    final val testAccount = Account(
        "Test Account",
        "test_account@test.com",
        "test_password",
        "This is a test account",
        TestUtils.createDate(2001, Calendar.JULY, 28),
        "https://test-photo.com",
        "https://linkedin.com",
        "https://github.com",
        listOf(
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "test")
        )
    )

    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        mutableListOf(testAccount),
        mutableListOf(),
        "great-event",
        "cool-image.png",
        mutableListOf(),
        "https://docs.google.com/forms",
        DateInterval(
            TestUtils.createDate(2022, Calendar.JULY, 28),
            TestUtils.createDate(2022, Calendar.JULY, 30)
        ),
        "FEUP",
        "Great Events"
    )

    val documentation = PayloadEvent()

    @DisplayName("GET events?category={category}")
    @Nested
    inner class GetEvents {
        private val testEvents = mutableListOf(
            testEvent
        )

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events`() {
            mockMvc.perform(get("/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(testEvents)))
                .andDocument(
                    documentation.getModelDocumentationArray(),
                    "Get all the events or filter them by category",
                    """The operation returns an array of events, allowing to easily retrieve all the created events.
                        |It also allows to filter the events by category, using the query parameter "category".
                        |This is useful for example in the frontend's event page, where events are displayed.
                    """.trimMargin()
                )
        }

        private val queryParameters = listOf(
            parameterWithName("category").description("Category of the events to retrieve").optional()
        )

        @Test
        fun `should return all events of the category`() {
            val extraEvents = listOf(
                Event(
                    "Mid event",
                    "This event was ok",
                    mutableListOf(),
                    mutableListOf(),
                    "bloat",
                    "waldo.jpeg",
                    null,
                    DateInterval(
                        TestUtils.createDate(2022, Calendar.JANUARY, 15),
                        null
                    ),
                    "FCUP",
                    "Other category"
                ),
                Event(
                    "Cool event",
                    "This event was a awesome",
                    mutableListOf(testAccount),
                    mutableListOf(),
                    "ni",
                    "ni.png",
                    null,
                    DateInterval(
                        TestUtils.createDate(2022, Calendar.SEPTEMBER, 11),
                        null
                    ),
                    "NI",
                    "Great Events"
                )
            )
            extraEvents.forEach { repository.save(it) }

            mockMvc.perform(get("/events?category={category}", testEvent.category))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.length()").value(2),
                    jsonPath("$[0].category").value(testEvent.category),
                    jsonPath("$[1].category").value(testEvent.category)
                )
                .andDocument(
                    documentation.getModelDocumentationArray(),
                    queryParameters = queryParameters
                )
        }
    }

    @NestedTest
    @DisplayName("GET /events/{id}")
    inner class GetEventById {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        private val parameters = listOf(parameterWithName("id").description("ID of the event to retrieve"))

        @Test
        fun `should return the event`() {
            mockMvc.perform(get("/events/{id}", testEvent.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.registerUrl").value(testEvent.registerUrl),
                    jsonPath("$.dateInterval.startDate").value(testEvent.dateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(testEvent.dateInterval.endDate.toJson()),
                    jsonPath("$.location").value(testEvent.location),
                    jsonPath("$.category").value(testEvent.category),
                    jsonPath("$.image").value(testEvent.image),
                    jsonPath("$.slug").value(testEvent.slug)

                )
                .andDocument(
                    documentation,
                    "Get events by ID",
                    "This endpoint allows the retrieval of a single event using its ID. " +
                        "It might be used to generate the specific event page.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.perform(get("/events/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234")
                )
                .andDocumentErrorResponse(documentation)
        }
    }

    @NestedTest
    @DisplayName("GET /events/{eventSlug}")
    inner class GetEventBySlug {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        private val parameters = listOf(
            parameterWithName("slug").description("Short and friendly textual event identifier")
        )

        @Test
        fun `should return the event`() {
            mockMvc.perform(get("/events/{slug}", testEvent.slug))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.registerUrl").value(testEvent.registerUrl),
                    jsonPath("$.dateInterval.startDate").value(testEvent.dateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(testEvent.dateInterval.endDate.toJson()),
                    jsonPath("$.location").value(testEvent.location),
                    jsonPath("$.category").value(testEvent.category),
                    jsonPath("$.image").value(testEvent.image),
                    jsonPath("$.slug").value(testEvent.slug)
                )
                .andDocument(
                    documentation,
                    "Get events by slug",
                    "This endpoint allows the retrieval of a single event using its slug.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the event slug does not exist`() {
            mockMvc.perform(get("/events/{slug}", "fail-slug"))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with slug fail-slug")
                )
                .andDocumentErrorResponse(documentation, urlParameters = parameters)
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
                mutableListOf(testAccount),
                mutableListOf(),
                null,
                "bad-image.png",
                mutableListOf(),
                null,
                DateInterval(
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                    null
                ),
                null,
                null
            ),
            Event(
                "Mid event",
                "This event was ok",
                mutableListOf(),
                mutableListOf(),
                null,
                "mid-image.png",
                mutableListOf(),
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.JANUARY, 15),
                    null
                ),
                null,
                "Other category"
            ),
            Event(
                "Cool event",
                "This event was a awesome",
                mutableListOf(testAccount),
                mutableListOf(),
                null,
                "cool-image.png",
                mutableListOf(),
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.SEPTEMBER, 11),
                    null
                ),
                null,
                "Great Events"
            )
        )

        private val parameters = listOf(parameterWithName("category").description("Category of the events to retrieve"))

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            for (event in testEvents) repository.save(event)
        }

        @Test
        fun `should return all events of the category`() {
            mockMvc.perform(get("/events/category/{category}", testEvent.category))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.length()").value(2),
                    jsonPath("$[0].category").value(testEvent.category),
                    jsonPath("$[1].category").value(testEvent.category)
                )
                .andDocument(
                    documentation.getModelDocumentationArray(),
                    "Get events by category",
                    "This endpoint allows the retrieval of events labeled with a given category. " +
                        "It might be used to filter events in the event page.",
                    urlParameters = parameters
                )
        }
    }

    @NestedTest
    @DisplayName("POST /events")
    inner class CreateEvent {
        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)
        private val expectedImagePath = "${uploadConfigProperties.staticServe}/events/${testEvent.title}-$uuid.jpeg"

        @BeforeEach
        fun addAccount() {
            accountRepository.save(testAccount)
        }

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @Test
        fun `should create a new event`() {
            val eventPart = objectMapper.writeValueAsString(
                mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "teamMembersIds" to mutableListOf(testAccount.id!!),
                    "registerUrl" to testEvent.registerUrl,
                    "location" to testEvent.location,
                    "category" to testEvent.category,
                    "slug" to testEvent.slug
                )
            )

            mockMvc.multipartBuilder("/events")
                .addPart("event", eventPart)
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.teamMembers.length()").value(1),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.dateInterval.startDate").value(testEvent.dateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(testEvent.dateInterval.endDate.toJson()),
                    jsonPath("$.location").value(testEvent.location),
                    jsonPath("$.category").value(testEvent.category),
                    jsonPath("$.image").value(expectedImagePath),
                    jsonPath("$.slug").value(testEvent.slug)
                )
//                .andDocument(
//                    documentation,
//                    "Create new events",
//                    "This endpoint operation creates a new event.",
//                    documentRequestPayload = true
//                )
        }

        @Test
        fun `should fail if slug already exists`() {
            val duplicatedSlugEvent = Event(
                "Duplicated slug",
                "This have a duplicated slug",
                mutableListOf(testAccount),
                mutableListOf(),
                testEvent.slug,
                "duplicated-slug.png",
                mutableListOf(),
                "https://docs.google.com/forms",
                DateInterval(
                    TestUtils.createDate(2022, Calendar.AUGUST, 28),
                    TestUtils.createDate(2022, Calendar.AUGUST, 30)
                ),
                "FEUP",
                "Great Events"
            )

            mockMvc.multipartBuilder("/events")
                .addPart("event", objectMapper.writeValueAsString(testEvent))
                .addFile(name = "image")
                .perform()
                .andExpect { status().isOk }

            mockMvc.multipartBuilder("/events")
                .addPart("event", objectMapper.writeValueAsString(duplicatedSlugEvent))
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to create event with invalid filename extension`() {
            mockMvc.multipartBuilder("/events")
                .addPart("event", objectMapper.writeValueAsString(testEvent))
                .addFile(name = "image", filename = "image.pdf")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("createEvent.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to create event with invalid filename media type`() {
            mockMvc.multipartBuilder("/events")
                .addPart("event", objectMapper.writeValueAsString(testEvent))
                .addFile(name = "image", contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("createEvent.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail when missing event part`() {
            mockMvc.multipartBuilder("/events")
                .addFile(name = "image")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("required"),
                    jsonPath("$.errors[0].param").value("event")
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.multipartBuilder("/events")
                        .addPart("event", objectMapper.writeValueAsString(params))
                        .addFile(name = "image")
                        .perform()
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "image" to testEvent.image,
                    "slug" to testEvent.slug
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Title.minSize}" +
                        " and ${ActivityConstants.Title.maxSize}()"
                )
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Description.minSize}" +
                        " and ${ActivityConstants.Description.maxSize}()"
                )
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
            @DisplayName("slug")
            inner class SlugValidation {
                @BeforeAll
                fun setParam() {
                    validationTester.param = "slug"
                }

                @Test
                @DisplayName(
                    "size should be between ${ActivityConstants.Slug.minSize} and ${ActivityConstants.Slug.maxSize}()"
                )
                fun size() = validationTester.hasSizeBetween(
                    ActivityConstants.Slug.minSize,
                    ActivityConstants.Slug.maxSize
                )
            }
        }
    }

    @NestedTest
    @DisplayName("DELETE /events/{eventId}")
    inner class DeleteEvent {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        private val parameters = listOf(parameterWithName("id").description("ID of the event to delete"))

        @Test
        fun `should delete the event`() {
            mockMvc.perform(delete("/events/{id}", testEvent.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty
                )
                .andDocumentEmptyObjectResponse(
                    documentation,
                    "Delete events",
                    "This operation deletes an event using its ID.",
                    urlParameters = parameters
                )

            assert(repository.findById(testEvent.id!!).isEmpty)
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.perform(delete("/events/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234")
                )
                .andDocumentErrorResponse(documentation, urlParameters = parameters)
        }
    }

    @NestedTest
    @DisplayName("PUT /events/{eventId}/team/{accountId}")
    inner class AddTeamMember {

        private val newAccount = Account(
            "Another test Account",
            "test2_account@test.com",
            "test_password",
            "This is another test account",
            TestUtils.createDate(2003, Calendar.APRIL, 4),
            "https://test-photo.com",
            "https://linkedin.com",
            "https://github.com",
            listOf(
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png", "test")
            )
        )

        @BeforeEach
        fun addAccounts() {
            accountRepository.save(testAccount)
            accountRepository.save(newAccount)
            repository.save(testEvent)
        }

        private val parameters = listOf(
            parameterWithName("eventId")
                .description("ID of the event to add the member to"),
            parameterWithName("accountId").description("ID of the account to add")
        )

        @Test
        fun `should add a team member`() {
            mockMvc.perform(put("/events/{eventId}/team/{accountId}", testEvent.id, newAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(2),
                    jsonPath("$.teamMembers.length()").value(2),
                    jsonPath("$.teamMembers[0].name").value(testAccount.name),
                    jsonPath("$.teamMembers[0].email").value(testAccount.email),
                    jsonPath("$.teamMembers[0].bio").value(testAccount.bio),
                    jsonPath("$.teamMembers[0].birthDate").value(testAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[0].linkedin").value(testAccount.linkedin),
                    jsonPath("$.teamMembers[0].github").value(testAccount.github),
                    jsonPath("$.teamMembers[0].websites.length()").value(1),
                    jsonPath("$.teamMembers[0].websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.teamMembers[0].websites[0].iconPath").value(testAccount.websites[0].iconPath),
                    jsonPath("$.teamMembers[1].name").value(newAccount.name),
                    jsonPath("$.teamMembers[1].email").value(newAccount.email),
                    jsonPath("$.teamMembers[1].bio").value(newAccount.bio),
                    jsonPath("$.teamMembers[1].birthDate").value(newAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[1].linkedin").value(newAccount.linkedin),
                    jsonPath("$.teamMembers[1].github").value(newAccount.github),
                    jsonPath("$.teamMembers[1].websites.length()").value(1),
                    jsonPath("$.teamMembers[1].websites[0].url").value(newAccount.websites[0].url),
                    jsonPath("$.teamMembers[1].websites[0].iconPath").value(newAccount.websites[0].iconPath)
                )
                .andDocument(
                    documentation,
                    "Add team member to event",
                    "This operation adds a team member to a given event.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/events/{eventId}/team/{accountId}", testEvent.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
                .andDocumentErrorResponse(documentation, urlParameters = parameters)
        }
    }

    @NestedTest
    @DisplayName("DELETE /events/{projectId}/team/{accountId}")
    inner class RemoveTeamMember {

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        private val parameters = listOf(
            parameterWithName("eventId")
                .description("ID of the event to remove the member from"),
            parameterWithName("accountId").description("ID of the account to remove")
        )

        @Test
        fun `should remove a team member`() {
            mockMvc.perform(delete("/events/{eventId}/team/{accountId}", testEvent.id, testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(0)
                )
                .andDocument(
                    documentation,
                    "Remove a team member from event",
                    "This operation removes a team member of a given event.",
                    urlParameters = parameters
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(delete("/events/{eventId}/team/{accountId}", testEvent.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234")
                )
                .andDocumentErrorResponse(documentation, urlParameters = parameters)
        }
    }

    @NestedTest
    @DisplayName("PUT /events/{idEvent}/gallery/addPhoto")
    inner class AddGalleryPhoto {

        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        @Test
        fun `should add a photo`() {
            val expectedPhotoPath = "${uploadConfigProperties.staticServe}/gallery/${testEvent.title}-$uuid.jpeg"

            mockMvc.multipartBuilder("/events/${testEvent.id}/gallery/addPhoto")
                .asPutMethod()
                .addFile("image", contentType = MediaType.IMAGE_JPEG_VALUE)
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.teamMembers.length()").value(testEvent.teamMembers.size),
                    jsonPath("$.gallery.length()").value(1),
                    jsonPath("$.gallery[0]").value(expectedPhotoPath),
                    jsonPath("$.registerUrl").value(testEvent.registerUrl),
                    jsonPath("$.dateInterval.startDate").value(testEvent.dateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(testEvent.dateInterval.endDate.toJson()),
                    jsonPath("$.location").value(testEvent.location),
                    jsonPath("$.category").value(testEvent.category),
                    jsonPath("$.slug").value(testEvent.slug),
                    jsonPath("$.image").value(testEvent.image)
                )
        }

        @Test
        fun `should fail if event does not exist`() {
            val unexistentID = 5

            mockMvc.multipartBuilder("/events/$unexistentID/gallery/addPhoto")
                .asPutMethod()
                .addFile("image", contentType = MediaType.IMAGE_JPEG_VALUE)
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id $unexistentID")
                )
        }

        @Test
        fun `should fail if image in wrong format`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}/gallery/addPhoto")
                .asPutMethod()
                .addFile("image", filename = "image.gif", contentType = MediaType.IMAGE_JPEG_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)")
                )
        }
    }

    @NestedTest
    @DisplayName("PUT /events/{idEvent}/gallery/removePhoto")
    inner class RemoveGalleryPhoto {

        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)
        private val mockPhotoUrl = "${uploadConfigProperties.staticServe}/gallery/${testEvent.title}-$uuid.jpeg"

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)

            val testEventClone = testEvent

            testEvent.gallery.add(mockPhotoUrl)
            repository.save(testEventClone)
        }

        @Test
        fun `should remove a photo`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}/gallery/removePhoto")
                .asPutMethod()
                .addPart("photoUrl", mockPhotoUrl)
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(testEvent.title),
                    jsonPath("$.description").value(testEvent.description),
                    jsonPath("$.teamMembers.length()").value(testEvent.teamMembers.size),
                    jsonPath("$.gallery.length()").value(0),
                    jsonPath("$.registerUrl").value(testEvent.registerUrl),
                    jsonPath("$.dateInterval.startDate").value(testEvent.dateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(testEvent.dateInterval.endDate.toJson()),
                    jsonPath("$.location").value(testEvent.location),
                    jsonPath("$.category").value(testEvent.category),
                    jsonPath("$.slug").value(testEvent.slug),
                    jsonPath("$.image").value(testEvent.image)
                )
        }

        @Test
        fun `should fail if event does not exist`() {
            val unexistentID = 5

            mockMvc.multipartBuilder("/events/$unexistentID/gallery/removePhoto")
                .asPutMethod()
                .addPart("photoUrl", mockPhotoUrl)
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id $unexistentID")
                )
        }

        @Test
        fun `should fail if image does not exist`() {
            val wrongPhotoUrl = "${uploadConfigProperties.staticServe}/gallery/Another${testEvent.title}-$uuid.jpeg"

            mockMvc.multipartBuilder("/events/${testEvent.id}/gallery/removePhoto")
                .asPutMethod()
                .addPart("photoUrl", wrongPhotoUrl)
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("photo not found")
                )
        }
    }

    @NestedTest
    @DisplayName("PUT /events/{eventId}")
    inner class UpdateEvent {
        private val testAccount2 = Account(
            "Test Account2",
            "test_account2@test.com",
            "test_password",
            "This is a test account2",
            TestUtils.createDate(2001, Calendar.JULY, 28),
            "https://test-photo.com",
            "https://linkedin.com",
            "https://github.com"
        )

        private val uuid: UUID = UUID.randomUUID()
        private val mockedSettings = Mockito.mockStatic(UUID::class.java)

        private val newTitle = "New event title"
        private val newDescription = "New event description"
        private val newTeamMembers = mutableListOf<Long>()
        private val newRegisterUrl = "https://example.com/newUrl"
        private val newDateInterval = DateInterval(
            TestUtils.createDate(2022, Calendar.DECEMBER, 1),
            TestUtils.createDate(2022, Calendar.DECEMBER, 2)
        )
        private val newLocation = "FLUP"
        private val newCategory = "Greatest Events"
        private val newSlug = "new-slug"

        private val parameters = listOf(parameterWithName("id").description("ID of the event to update"))
        private lateinit var eventPart: MutableMap<String, Any>

        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            accountRepository.save(testAccount2)
            repository.save(testEvent)

            newTeamMembers.clear()
            newTeamMembers.add(testAccount2.id!!)
            eventPart = mutableMapOf(
                "title" to newTitle,
                "description" to newDescription,
                "teamMembersIds" to newTeamMembers,
                "registerUrl" to newRegisterUrl,
                "dateInterval" to newDateInterval,
                "location" to newLocation,
                "category" to newCategory,
                "slug" to newSlug
            )
        }

        @BeforeAll
        fun setupMocks() {
            Mockito.`when`(UUID.randomUUID()).thenReturn(uuid)
        }

        @AfterAll
        fun cleanup() {
            mockedSettings.close()
        }

        @Test
        fun `should update the event without image`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.teamMembers[0].id").value(testAccount2.id),
                    jsonPath("$.registerUrl").value(newRegisterUrl),
                    jsonPath("$.dateInterval.startDate").value(newDateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(newDateInterval.endDate.toJson()),
                    jsonPath("$.location").value(newLocation),
                    jsonPath("$.category").value(newCategory),
                    jsonPath("$.slug").value(newSlug),
                    jsonPath("$.image").value(testEvent.image)
                )
//                .andDocument(
//                    documentation,
//                    "Update events",
//                    "Update a previously created event, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedEvent = repository.findById(testEvent.id!!).get()
            assertEquals(newTitle, updatedEvent.title)
            assertEquals(newDescription, updatedEvent.description)
            assertEquals(newRegisterUrl, updatedEvent.registerUrl)
            assertEquals(newDateInterval.startDate.toJson(), updatedEvent.dateInterval.startDate.toJson())
            assertEquals(newDateInterval.endDate.toJson(), updatedEvent.dateInterval.endDate.toJson())
            assertEquals(newLocation, updatedEvent.location)
            assertEquals(newCategory, updatedEvent.category)
            assertEquals(newSlug, updatedEvent.slug)
            assertEquals(testEvent.image, updatedEvent.image)
            assertEquals(newTeamMembers.size, updatedEvent.teamMembers.size)
            assertEquals(testAccount2.id, updatedEvent.teamMembers[0].id)
        }

        @Test
        fun `should update the event with same slug`() {
            eventPart["slug"] = testEvent.slug!!
            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.registerUrl").value(newRegisterUrl),
                    jsonPath("$.dateInterval.startDate").value(newDateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(newDateInterval.endDate.toJson()),
                    jsonPath("$.location").value(newLocation),
                    jsonPath("$.category").value(newCategory),
                    jsonPath("$.slug").value(testEvent.slug)
                )
        }

        @Test
        fun `should fail to update if the slug already exists`() {
            val otherEvent = Event(
                title = newTitle,
                description = newDescription,
                teamMembers = mutableListOf(),
                registerUrl = newRegisterUrl,
                dateInterval = DateInterval(
                    TestUtils.createDate(2022, Calendar.DECEMBER, 1),
                    TestUtils.createDate(2022, Calendar.DECEMBER, 2)
                ),
                location = newLocation,
                category = newCategory,
                image = "image.png",
                slug = newSlug,
                gallery = mutableListOf()
            )
            repository.save(otherEvent)

            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .perform()
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should update the event with image`() {
            val expectedImagePath = "${uploadConfigProperties.staticServe}/events/$newTitle-$uuid.jpeg"

            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .addFile("image", "new-image.jpeg", contentType = MediaType.IMAGE_JPEG_VALUE)
                .perform()
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(newTeamMembers.size),
                    jsonPath("$.registerUrl").value(newRegisterUrl),
                    jsonPath("$.dateInterval.startDate").value(newDateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(newDateInterval.endDate.toJson()),
                    jsonPath("$.location").value(newLocation),
                    jsonPath("$.category").value(newCategory),
                    jsonPath("$.slug").value(newSlug),
                    jsonPath("$.image").value(expectedImagePath)
                )
//                .andDocument(
//                    documentation,
//                    "Update events",
//                    "Update a previously created event and changes its image, using its ID.",
//                    urlParameters = parameters,
//                    documentRequestPayload = true
//                )

            val updatedEvent = repository.findById(testEvent.id!!).get()
            assertEquals(newTitle, updatedEvent.title)
            assertEquals(newDescription, updatedEvent.description)
            assertEquals(newRegisterUrl, updatedEvent.registerUrl)
            assertEquals(newDateInterval.startDate.toJson(), updatedEvent.dateInterval.startDate.toJson())
            assertEquals(newDateInterval.endDate.toJson(), updatedEvent.dateInterval.endDate.toJson())
            assertEquals(newLocation, updatedEvent.location)
            assertEquals(newCategory, updatedEvent.category)
            assertEquals(newSlug, updatedEvent.slug)
            assertEquals(expectedImagePath, updatedEvent.image)
        }

        @Test
        fun `should fail if the event does not exist`() {
            val eventPart = objectMapper.writeValueAsString(
                mapOf(
                    "title" to "New Title",
                    "description" to "New Description",
                    "dateInterval" to DateInterval(TestUtils.createDate(2022, Calendar.DECEMBER, 1), null),
                    "associatedRoles" to testEvent.associatedRoles
                )
            )

            mockMvc.multipartBuilder("/events/1234")
                .asPutMethod()
                .addPart("event", eventPart)
                .perform()
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to update event with invalid filename extension`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .addFile(name = "image", filename = "image.pdf")
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("updateEventById.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail to update event with invalid filename media type`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .addPart("event", objectMapper.writeValueAsString(eventPart))
                .addFile(name = "image", contentType = MediaType.APPLICATION_PDF_VALUE)
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("invalid image type (png, jpg,  jpeg or webp)"),
                    jsonPath("$.errors[0].param").value("updateEventById.image")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @Test
        fun `should fail when missing event part`() {
            mockMvc.multipartBuilder("/events/${testEvent.id}")
                .asPutMethod()
                .perform()
                .andExpectAll(
                    status().isBadRequest,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("required"),
                    jsonPath("$.errors[0].param").value("event")
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.multipartBuilder("/events/${testEvent.id}")
                        .asPutMethod()
                        .addPart("event", objectMapper.writeValueAsString(params))
                        .perform()
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "image" to testEvent.image
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Title.minSize}" +
                        " and ${ActivityConstants.Title.maxSize}()"
                )
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Title.minSize,
                        ActivityConstants.Title.maxSize
                    )
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
                @DisplayName(
                    "size should be between ${ActivityConstants.Description.minSize} " +
                        "and ${ActivityConstants.Description.maxSize}()"
                )
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
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
