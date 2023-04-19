package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Calendar
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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

@ControllerTest
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: EventRepository,
    val accountRepository: AccountRepository
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
        )
    )

    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        mutableListOf(testAccount),
        mutableListOf(),
        "great-event",
        "https://docs.google.com/forms",
        DateInterval(
            TestUtils.createDate(2022, Calendar.JULY, 28),
            TestUtils.createDate(2022, Calendar.JULY, 30)
        ),
        "FEUP",
        "Great Events",
        "https://example.com/exampleThumbnail"
    )

    val documentation = PayloadEvent()

    @NestedTest
    @DisplayName("GET /events")
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                mutableListOf(),
                mutableListOf(),
                null,
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
                    "Get all the events",
                    """The operation returns an array of events, allowing to easily retrieve all the created events.
                        |This is useful for example in the frontend's event page, where events are displayed.
                    """.trimMargin()
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
                    jsonPath("$.thumbnailPath").value(testEvent.thumbnailPath),
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
                    jsonPath("$.thumbnailPath").value(testEvent.thumbnailPath),
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
                mutableListOf(),
                mutableListOf(),
                null,
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
                mutableListOf(testAccount),
                mutableListOf(),
                null,
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
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @BeforeEach
        fun addAccount() {
            accountRepository.save(testAccount)
        }

        @Test
        fun `should create a new event`() {
            mockMvc.perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to testEvent.title,
                                "description" to testEvent.description,
                                "dateInterval" to testEvent.dateInterval,
                                "teamMembersIds" to mutableListOf(testAccount.id!!),
                                "registerUrl" to testEvent.registerUrl,
                                "location" to testEvent.location,
                                "category" to testEvent.category,
                                "thumbnailPath" to testEvent.thumbnailPath,
                                "slug" to testEvent.slug
                            )
                        )
                    )
            )
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
                    jsonPath("$.thumbnailPath").value(testEvent.thumbnailPath),
                    jsonPath("$.slug").value(testEvent.slug)
                )
                .andDocument(
                    documentation,
                    "Create new events",
                    "This endpoint operation creates a new event.",
                    documentRequestPayload = true
                )
        }

        @Test
        fun `should fail if slug already exists`() {
            val duplicatedSlugEvent = Event(
                "Duplicated slug",
                "This have a duplicated slug",
                mutableListOf(testAccount),
                mutableListOf(),
                testEvent.slug,
                "https://docs.google.com/forms",
                DateInterval(
                    TestUtils.createDate(2022, Calendar.AUGUST, 28),
                    TestUtils.createDate(2022, Calendar.AUGUST, 30)
                ),
                "FEUP",
                "Great Events",
                "https://example.com/exampleThumbnail"
            )

            mockMvc.post("/events/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(testEvent)
            }.andExpect { status { isOk() } }

            mockMvc.perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicatedSlugEvent))
            )
                .andExpectAll(
                    status().isUnprocessableEntity,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("slug already exists")
                )
                .andDocumentErrorResponse(documentation, hasRequestPayload = true)
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/events/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params))
                    )
                        .andDocumentErrorResponse(documentation, hasRequestPayload = true)
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "thumbnailPath" to testEvent.thumbnailPath,
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
    @DisplayName("PUT /events/{eventId}/addTeamMember/{accountId}")
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
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png")
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
            mockMvc.perform(put("/events/{eventId}/addTeamMember/{accountId}", testEvent.id, newAccount.id))
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
            mockMvc.perform(put("/events/{eventId}/addTeamMember/{accountId}", testEvent.id, 1234))
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
    @DisplayName("PUT /events/{projectId}/addTeamMember/{accountId}")
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
            mockMvc.perform(put("/events/{eventId}/removeTeamMember/{accountId}", testEvent.id, testAccount.id))
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
            mockMvc.perform(put("/events/{eventId}/removeTeamMember/{accountId}", testEvent.id, 1234))
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
    @DisplayName("PUT /events/{eventId}")
    inner class UpdateEvent {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        val parameters = listOf(parameterWithName("id").description("ID of the event to update"))

        @Test
        fun `should update the event`() {
            val newTitle = "New event title"
            val newDescription = "New event description"
            val newTeamMembers = mutableListOf<Long>()
            val newRegisterUrl = "https://example.com/newUrl"
            val newDateInterval = DateInterval(
                TestUtils.createDate(2022, Calendar.DECEMBER, 1),
                TestUtils.createDate(2022, Calendar.DECEMBER, 2)
            )
            val newLocation = "FLUP"
            val newCategory = "Greatest Events"
            val newThumbnailPath = "https://thumbnails/new.png"
            val newSlug = "new-slug"

            mockMvc.perform(
                put("/events/{id}", testEvent.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "teamMembersIds" to newTeamMembers,
                                "registerUrl" to newRegisterUrl,
                                "dateInterval" to newDateInterval,
                                "location" to newLocation,
                                "category" to newCategory,
                                "thumbnailPath" to newThumbnailPath,
                                "slug" to newSlug
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(newTitle),
                    jsonPath("$.description").value(newDescription),
                    jsonPath("$.teamMembers.length()").value(0),
                    jsonPath("$.registerUrl").value(newRegisterUrl),
                    jsonPath("$.dateInterval.startDate").value(newDateInterval.startDate.toJson()),
                    jsonPath("$.dateInterval.endDate").value(newDateInterval.endDate.toJson()),
                    jsonPath("$.location").value(newLocation),
                    jsonPath("$.category").value(newCategory),
                    jsonPath("$.thumbnailPath").value(newThumbnailPath),
                    jsonPath("$.slug").value(newSlug)

                )
                .andDocument(
                    documentation,
                    "Update events",
                    "Update a previously created event, using its ID.",
                    urlParameters = parameters,
                    documentRequestPayload = true
                )

            val updatedEvent = repository.findById(testEvent.id!!).get()
            assertEquals(newTitle, updatedEvent.title)
            assertEquals(newDescription, updatedEvent.description)
            assertEquals(newRegisterUrl, updatedEvent.registerUrl)
            assertEquals(newDateInterval.startDate.toJson(), updatedEvent.dateInterval.startDate.toJson())
            assertEquals(newDateInterval.endDate.toJson(), updatedEvent.dateInterval.endDate.toJson())
            assertEquals(newLocation, updatedEvent.location)
            assertEquals(newCategory, updatedEvent.category)
            assertEquals(newThumbnailPath, updatedEvent.thumbnailPath)
            assertEquals(newSlug, updatedEvent.slug)
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.perform(
                put("/events/{id}", 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "title" to "New Title",
                                "description" to "New Description",
                                "dateInterval" to DateInterval(TestUtils.createDate(2022, Calendar.DECEMBER, 1), null),
                                "thumbnailPath" to "http://test.com/thumbnail/1",
                                "associatedRoles" to testEvent.associatedRoles
                            )
                        )
                    )
            )
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234")
                )
                .andDocumentErrorResponse(documentation, urlParameters = parameters, hasRequestPayload = true)
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        put("/events/{id}", testEvent.id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params))
                    )
                        .andDocumentErrorResponse(documentation, urlParameters = parameters, hasRequestPayload = true)
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
