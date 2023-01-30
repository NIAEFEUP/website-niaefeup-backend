package pt.up.fe.ni.website.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import pt.up.fe.ni.website.backend.model.Account
import pt.up.fe.ni.website.backend.model.CustomWebsite
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.repository.AccountRepository
import pt.up.fe.ni.website.backend.repository.EventRepository
import pt.up.fe.ni.website.backend.utils.TestUtils
import pt.up.fe.ni.website.backend.utils.ValidationTester
import pt.up.fe.ni.website.backend.utils.annotations.ControllerTest
import pt.up.fe.ni.website.backend.utils.annotations.EndpointTest
import pt.up.fe.ni.website.backend.utils.annotations.NestedTest
import java.util.Calendar
import java.util.Date
import pt.up.fe.ni.website.backend.model.constants.ActivityConstants as Constants

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
        "https://docs.google.com/forms",
        TestUtils.createDate(2022, Calendar.JULY, 28),
        mutableListOf(testAccount)
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
                mutableListOf()
            )
        )

        @BeforeAll
        fun addEvents() {
            accountRepository.save(testAccount)
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
    @DisplayName("POST /events/new")
    inner class CreateEvent {
        @BeforeAll
        fun addAccount() {
            accountRepository.save(testAccount)
        }

        @Test
        fun `should create a new event`() {
            mockMvc.post("/events/new") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf(
                        "title" to testEvent.title,
                        "description" to testEvent.description,
                        "date" to testEvent.date,
                        "teamMembersIds" to mutableListOf(testAccount.id!!)
                    )
                )
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.title") { value(testEvent.title) }
                    jsonPath("$.description") { value(testEvent.description) }
                    jsonPath("$.registerUrl") { value(testEvent.registerUrl) }
                    jsonPath("$.date") { value(containsString("28-07-2022")) }
                    jsonPath("$.teamMembers.length()") { value(1) }
                    jsonPath("$.teamMembers[0].email") { value(testAccount.email) }
                    jsonPath("$.teamMembers[0].name") { value(testAccount.name) }
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
                    "date" to testEvent.date
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
                @DisplayName("size should be between ${Constants.Title.minSize} and ${Constants.Title.maxSize}()")
                fun size() = validationTester.hasSizeBetween(Constants.Title.minSize, Constants.Title.maxSize)
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
                @DisplayName("size should be between ${Constants.Description.minSize} and ${Constants.Description.maxSize}()")
                fun size() =
                    validationTester.hasSizeBetween(Constants.Description.minSize, Constants.Description.maxSize)
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
            @DisplayName("date")
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

    @EndpointTest
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

        @BeforeAll
        fun addEvent() {
            accountRepository.save(testAccount)
            accountRepository.save(newAccount)
            repository.save(testEvent)
        }

        @Test
        fun `should add a team member`() {
            mockMvc.put("/events/${testEvent.id}/addTeamMember/${newAccount.id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.teamMembers.length()") { value(2) }
                    jsonPath("$.teamMembers.length()") { value(2) }
                    jsonPath("$.teamMembers[0].name") { value(testAccount.name) }
                    jsonPath("$.teamMembers[0].email") { value(testAccount.email) }
                    jsonPath("$.teamMembers[0].bio") { value(testAccount.bio) }
                    jsonPath("$.teamMembers[0].birthDate") { value(testAccount.birthDate.toJson()) }
                    jsonPath("$.teamMembers[0].photoPath") { value(testAccount.photoPath) }
                    jsonPath("$.teamMembers[0].linkedin") { value(testAccount.linkedin) }
                    jsonPath("$.teamMembers[0].github") { value(testAccount.github) }
                    jsonPath("$.teamMembers[0].websites.length()") { value(1) }
                    jsonPath("$.teamMembers[0].websites[0].url") { value(testAccount.websites[0].url) }
                    jsonPath("$.teamMembers[0].websites[0].iconPath") { value(testAccount.websites[0].iconPath) }
                    jsonPath("$.teamMembers[1].name") { value(newAccount.name) }
                    jsonPath("$.teamMembers[1].email") { value(newAccount.email) }
                    jsonPath("$.teamMembers[1].bio") { value(newAccount.bio) }
                    jsonPath("$.teamMembers[1].birthDate") { value(newAccount.birthDate.toJson()) }
                    jsonPath("$.teamMembers[1].photoPath") { value(newAccount.photoPath) }
                    jsonPath("$.teamMembers[1].linkedin") { value(newAccount.linkedin) }
                    jsonPath("$.teamMembers[1].github") { value(newAccount.github) }
                    jsonPath("$.teamMembers[1].websites.length()") { value(1) }
                    jsonPath("$.teamMembers[1].websites[0].url") { value(newAccount.websites[0].url) }
                    jsonPath("$.teamMembers[1].websites[0].iconPath") { value(newAccount.websites[0].iconPath) }
                }
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.put("/events/${testEvent.id}/addTeamMember/1234")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("account not found with id 1234") }
                }
        }
    }

    @EndpointTest
    @DisplayName("PUT /events/{projectId}/addTeamMember/{accountId}")
    inner class RemoveTeamMember {

        @BeforeAll
        fun addToRepositories() {
            accountRepository.save(testAccount)
            repository.save(testEvent)
        }

        @Test
        fun `should remove a team member`() {
            mockMvc.put("/events/${testEvent.id}/removeTeamMember/${testAccount.id}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.teamMembers.length()") { value(0) }
                }
        }

        @Test
        fun `should fail if the team member does not exist`() {
            mockMvc.put("/events/${testEvent.id}/removeTeamMember/1234")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.errors.length()") { value(1) }
                    jsonPath("$.errors[0].message") { value("account not found with id 1234") }
                }
        }
    }

    fun Date?.toJson(): String {
        val quotedDate = objectMapper.writeValueAsString(this)
        // objectMapper adds quotes to the date, so remove them
        return quotedDate.substring(1, quotedDate.length - 1)
    }
}
