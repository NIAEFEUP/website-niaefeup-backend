package pt.up.fe.ni.website.backend.utils.mockmvc

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

class MockMvcMultipartBuilder(
    private val uri: String,
    private val mockMvc: MockMvc,
    ) {
    private val multipart = multipart(uri)

    fun addPart(key: String, data: String): MockMvcMultipartBuilder {
        val part = MockPart("dto", data.toByteArray())
        part.headers.contentType = MediaType.APPLICATION_JSON

        multipart.part(part)
        return this
    }

    fun addFile(
        name: String = "photo",
        filename: String = "photo.jpeg",
        content: String = "content",
        contentType: String = MediaType.IMAGE_JPEG_VALUE
    ): MockMvcMultipartBuilder {
        val file = MockMultipartFile(
            name,
            filename,
            contentType,
            content.toByteArray()
        )

        multipart.file(file)
        return this
    }

    public fun perform(): ResultActions {
        return mockMvc.perform(multipart)
    }
}
