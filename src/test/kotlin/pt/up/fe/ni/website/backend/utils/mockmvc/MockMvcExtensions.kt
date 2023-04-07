package pt.up.fe.ni.website.backend.utils.mockmvc

import org.springframework.test.web.servlet.MockMvc

fun MockMvc.multipartBuilder(s: String): MockMvcMultipartBuilder {
    return MockMvcMultipartBuilder(s, this)
}
