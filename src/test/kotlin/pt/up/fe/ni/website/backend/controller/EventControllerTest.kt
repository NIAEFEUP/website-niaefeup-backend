package pt.up.fe.ni.website.backend.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants
import pt.up.fe.ni.website.backend.model.embeddable.DateInterval
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import pt.up.fe.ni.website.backend.utils.documentation.DocumentationHelper.Companion.addFieldsToPayloadBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.EmptyObjectSchema
import pt.up.fe.ni.website.backend.utils.documentation.ErrorSchema
import pt.up.fe.ni.website.backend.utils.documentation.PayloadSchema
import java.util.Calendar
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.EventConstants as Constants

@ControllerTest
@AutoConfigureRestDocs
internal class EventControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val repository: EventRepository,
    val accountRepository: AccountRepository,
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
            CustomWebsite("https://test-website.com", "https://test-website.com/logo.png"),
        ),
        emptyList(),
    )

    val testEvent = Event(
        "Great event",
        "This was a nice and iconic event",
        mutableListOf(testAccount),
        "https://docs.google.com/forms",
        DateInterval(
            TestUtils.createDate(2022, Calendar.JULY, 28),
            TestUtils.createDate(2022, Calendar.JULY, 30),
        ),
        "FEUP",
        "Great Events",
        "https://example.com/exampleThumbnail",
    )

    private val eventFields = listOf<FieldDescriptor>(
        fieldWithPath("title").type(JsonFieldType.STRING).description("Event title"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("Event description"),
        fieldWithPath("thumbnailPath").type(JsonFieldType.STRING).description("Thumbnail of the event"),
        fieldWithPath("registerUrl").type(JsonFieldType.STRING).description("Link to the event registration").optional(),
        fieldWithPath("dateInterval.startDate").type(JsonFieldType.STRING).description("Event beginning date"),
        fieldWithPath("dateInterval.endDate").type(JsonFieldType.STRING).description("Event finishing date").optional(),
        fieldWithPath("location").type(JsonFieldType.STRING).description("Location for the event").optional(),
        fieldWithPath("category").type(JsonFieldType.STRING).description("Event category").optional(),
    )
    private val eventPayloadSchema = PayloadSchema("event", eventFields)
    private val responseOnlyEventFields = mutableListOf<FieldDescriptor>(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("Event ID"),
        fieldWithPath("teamMembers").type(JsonFieldType.ARRAY).description("Array of members associated with the event"),
        fieldWithPath("associatedRoles[]").description("Array of Roles/Activity associations"),
        fieldWithPath("associatedRoles[].*.role").type(JsonFieldType.OBJECT).description("Roles associated with the activity").optional(),
        fieldWithPath("associatedRoles[].*.activity").type(JsonFieldType.OBJECT).description("An activity that aggregates members with different roles").optional(),
        fieldWithPath("associatedRoles[].*.permissions").type(JsonFieldType.OBJECT).description("Permissions of someone with a given role for this activity").optional(),
        fieldWithPath("associatedRoles[].*.id").type(JsonFieldType.NUMBER).description("Id of the role/activity association").optional(),
    ).addFieldsToPayloadBeneathPath(
        "teamMembers",
        AccountControllerTest.accountPayloadSchema.Response().arrayDocumentedFields(AccountControllerTest.responseOnlyAccountFields),
        optional = true,
    )
    private val requestOnlyEventFields = listOf<FieldDescriptor>(
        fieldWithPath("teamMembersIds[]").type(JsonFieldType.ARRAY).description("Team member IDs"),
    )

    @NestedTest
    @DisplayName("GET /events")
    inner class GetAllEvents {
        private val testEvents = listOf(
            testEvent,
            Event(
                "Bad event",
                "This event was a failure",
                mutableListOf(),
                null,
                DateInterval(
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                    null,
                ),
                null,
                null,
                "https://example.com/exampleThumbnail2",
            ),
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
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Get all the events")
                                    .description(
                                        """
                                        The operation returns an array of events, allowing to easily retrieve all the created events.
                                        This is useful for example in the frontend's event page, where events are displayed.
                                        """.trimIndent(),
                                    )
                                    .responseSchema(eventPayloadSchema.Response().arraySchema())
                                    .responseFields(eventPayloadSchema.Response().arrayDocumentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }
    }

    @NestedTest
    @DisplayName("GET /events/{id}")
    inner class GetEvent {
        @BeforeEach
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

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
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .responseSchema(eventPayloadSchema.Response().schema())
                                    .responseFields(eventPayloadSchema.Response().documentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the event does not exist`() {
            mockMvc.perform(get("/events/{id}", 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234"),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Get events by ID")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of a single event using its ID.
                                        It might be used to generate the specific event page.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the event to retrieve"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
                null,
                DateInterval(
                    TestUtils.createDate(2021, Calendar.OCTOBER, 27),
                    null,
                ),
                null,
                null,
                "https://example.com/exampleThumbnail2",
            ),
            Event(
                "Mid event",
                "This event was ok",
                mutableListOf(),
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.JANUARY, 15),
                    null,
                ),
                null,
                "Other category",
                "https://example.com/exampleThumbnail2",
            ),
            Event(
                "Cool event",
                "This event was a awesome",
                mutableListOf(testAccount),
                null,
                DateInterval(
                    TestUtils.createDate(2022, Calendar.SEPTEMBER, 11),
                    null,
                ),
                null,
                "Great Events",
                "https://example.com/exampleThumbnail2",
            ),
        )

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
                    jsonPath("$[1].category").value(testEvent.category),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Get events by category")
                                    .description(
                                        """
                                        This endpoint allows the retrieval of events labeled with a given category.
                                        It might be used to filter events in the event page.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(parameterWithName("category").description("Category of the events to retrieve"))
                                    .responseSchema(eventPayloadSchema.Response().arraySchema())
                                    .responseFields(eventPayloadSchema.Response().arrayDocumentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
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
                            ),
                        ),
                    ),
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
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Create new events")
                                    .description(
                                        """
                                        This endpoint operation creates new events.
                                        """.trimIndent(),
                                    )
                                    .requestSchema(eventPayloadSchema.Request().schema())
                                    .requestFields(eventPayloadSchema.Request().documentedFields(requestOnlyEventFields))
                                    .responseSchema(eventPayloadSchema.Response().schema())
                                    .responseFields(eventPayloadSchema.Response().documentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        post("/events/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    ).andDo(
                        document(
                            "events/{ClassName}/{methodName}",
                            snippets = arrayOf(
                                resource(
                                    builder()
                                        .requestSchema(eventPayloadSchema.Request().schema())
                                        .responseSchema(ErrorSchema().Response().schema())
                                        .responseFields(ErrorSchema().Response().documentedFields())
                                        .tag("Events")
                                        .build(),
                                ),
                            ),
                        ),
                    )
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "thumbnailPath" to testEvent.thumbnailPath,
                ),
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
                        ActivityConstants.Description.maxSize,
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
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        @Test
        fun `should delete the event`() {
            mockMvc.perform(delete("/events/{id}", testEvent.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isEmpty,
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Delete events")
                                    .description(
                                        """
                                        This operation deletes an event using its ID.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the event to delete"))
                                    .responseSchema(EmptyObjectSchema().Response().schema())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
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
                    jsonPath("$.errors[0].message").value("event not found with id 1234"),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .pathParameters(parameterWithName("id").description("ID of the event to delete"))
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
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
                CustomWebsite("https://test-website.com", "https://test-website.com/logo.png"),
            ),
            emptyList(),
        )

        @BeforeEach
        fun addAccounts() {
            accountRepository.save(testAccount)
            accountRepository.save(newAccount)
            repository.save(testEvent)
        }

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
                    jsonPath("$.teamMembers[0].photoPath").value(testAccount.photoPath),
                    jsonPath("$.teamMembers[0].linkedin").value(testAccount.linkedin),
                    jsonPath("$.teamMembers[0].github").value(testAccount.github),
                    jsonPath("$.teamMembers[0].websites.length()").value(1),
                    jsonPath("$.teamMembers[0].websites[0].url").value(testAccount.websites[0].url),
                    jsonPath("$.teamMembers[0].websites[0].iconPath").value(testAccount.websites[0].iconPath),
                    jsonPath("$.teamMembers[1].name").value(newAccount.name),
                    jsonPath("$.teamMembers[1].email").value(newAccount.email),
                    jsonPath("$.teamMembers[1].bio").value(newAccount.bio),
                    jsonPath("$.teamMembers[1].birthDate").value(newAccount.birthDate.toJson()),
                    jsonPath("$.teamMembers[1].photoPath").value(newAccount.photoPath),
                    jsonPath("$.teamMembers[1].linkedin").value(newAccount.linkedin),
                    jsonPath("$.teamMembers[1].github").value(newAccount.github),
                    jsonPath("$.teamMembers[1].websites.length()").value(1),
                    jsonPath("$.teamMembers[1].websites[0].url").value(newAccount.websites[0].url),
                    jsonPath("$.teamMembers[1].websites[0].iconPath").value(newAccount.websites[0].iconPath),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Add member to event")
                                    .description(
                                        """
                                        This operation adds a member to a given event.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(
                                        parameterWithName("eventId").description("ID of the event to add the member to"),
                                        parameterWithName("accountId").description("ID of the account to add"),
                                    )
                                    .responseSchema(eventPayloadSchema.Response().schema())
                                    .responseFields(eventPayloadSchema.Response().documentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/events/{eventId}/addTeamMember/{accountId}", testEvent.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234"),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .pathParameters(
                                        parameterWithName("eventId").description("ID of the event to add the member to"),
                                        parameterWithName("accountId").description("ID of the account to add"),
                                    )
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
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

        @Test
        fun `should remove a team member`() {
            mockMvc.perform(put("/events/{eventId}/removeTeamMember/{accountId}", testEvent.id, testAccount.id))
                .andExpectAll(
                    status().isOk,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.teamMembers.length()").value(0),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Remove member from event")
                                    .description(
                                        """
                                        This operation removes a member of a given event.
                                        """.trimIndent(),
                                    )
                                    .pathParameters(
                                        parameterWithName("eventId").description("ID of the event to remove the member from"),
                                        parameterWithName("accountId").description("ID of the account to remove"),
                                    )
                                    .responseSchema(eventPayloadSchema.Response().schema())
                                    .responseFields(eventPayloadSchema.Response().documentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.perform(put("/events/{eventId}/removeTeamMember/{accountId}", testEvent.id, 1234))
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("account not found with id 1234"),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .pathParameters(
                                        parameterWithName("eventId").description("ID of the event to remove the member from"),
                                        parameterWithName("accountId").description("ID of the account to remove"),
                                    )
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
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

        @Test
        fun `should update the event`() {
            val newTitle = "New event title"
            val newDescription = "New event description"
            val newTeamMembers = mutableListOf<Long>()
            val newRegisterUrl = "https://example.com/newUrl"
            val newDateInterval = DateInterval(
                TestUtils.createDate(2022, Calendar.DECEMBER, 1),
                TestUtils.createDate(2022, Calendar.DECEMBER, 2),
            )
            val newLocation = "FLUP"
            val newCategory = "Greatest Events"
            val newThumbnailPath = "https://thumbnails/new.png"

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
                            ),
                        ),
                    ),
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
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .summary("Update events")
                                    .description(
                                        """Update previously created events, using their ID.""",
                                    )
                                    .pathParameters(parameterWithName("id").description("ID of the event to update"))
                                    .requestSchema(eventPayloadSchema.Request().schema())
                                    .requestFields(eventPayloadSchema.Request().documentedFields(requestOnlyEventFields))
                                    .responseSchema(eventPayloadSchema.Response().schema())
                                    .responseFields(eventPayloadSchema.Response().documentedFields(responseOnlyEventFields))
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
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
                                "associatedRoles" to testEvent.associatedRoles,
                            ),
                        ),
                    ),
            )
                .andExpectAll(
                    status().isNotFound,
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.errors.length()").value(1),
                    jsonPath("$.errors[0].message").value("event not found with id 1234"),
                )
                .andDo(
                    document(
                        "events/{ClassName}/{methodName}",
                        snippets = arrayOf(
                            resource(
                                builder()
                                    .pathParameters(parameterWithName("id").description("ID of the event to update"))
                                    .requestSchema(eventPayloadSchema.Request().schema())
                                    .responseSchema(ErrorSchema().Response().schema())
                                    .responseFields(ErrorSchema().Response().documentedFields())
                                    .tag("Events")
                                    .build(),
                            ),
                        ),
                    ),
                )
        }

        @NestedTest
        @DisplayName("Input Validation")
        inner class InputValidation {
            private val validationTester = ValidationTester(
                req = { params: Map<String, Any?> ->
                    mockMvc.perform(
                        put("/events/{id}", testEvent.id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)),
                    )
                        .andDo(
                            document(
                                "events/{ClassName}/{methodName}",
                                snippets = arrayOf(
                                    resource(
                                        builder()
                                            .pathParameters(parameterWithName("id").description("ID of the events to update"))
                                            .requestSchema(eventPayloadSchema.Request().schema())
                                            .responseSchema(ErrorSchema().Response().schema())
                                            .responseFields(ErrorSchema().Response().documentedFields())
                                            .tag("Events")
                                            .build(),
                                    ),
                                ),
                            ),
                        )
                },
                requiredFields = mapOf(
                    "title" to testEvent.title,
                    "description" to testEvent.description,
                    "dateInterval" to testEvent.dateInterval,
                    "thumbnailPath" to testEvent.thumbnailPath,
                ),
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
                    validationTester.hasSizeBetween(
                        ActivityConstants.Title.minSize,
                        ActivityConstants.Title.maxSize,
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
                @DisplayName("size should be between ${ActivityConstants.Description.minSize} and ${ActivityConstants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(
                        ActivityConstants.Description.minSize,
                        ActivityConstants.Description.maxSize,
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
