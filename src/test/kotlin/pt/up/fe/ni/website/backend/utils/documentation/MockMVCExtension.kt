package pt.up.fe.ni.website.backend.utils.documentation

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.ResourceDocumentation
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.test.web.servlet.ResultActions
import pt.up.fe.ni.website.backend.documentation.payloadschemas.EmptyObjectSchema
import pt.up.fe.ni.website.backend.documentation.payloadschemas.ErrorSchema

class MockMVCExtension {
    companion object {
        private val emptyPayload = EmptyObjectSchema()
        private val errorPayloadSchema = ErrorSchema()

        fun ResultActions.andDocument(
            documentation: ModelDocumentation,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            responseHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                documentation.payload.Request().schema(),
                documentation.payload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                documentation.payload.Response().schema(),
                documentation.payload.Response().getSchemaFieldDescriptors(),
                responseHeaders,
                documentRequestPayload,
                hasRequestPayload
            )
        }

        fun ResultActions.andDocumentCustomRequestSchema(
            documentation: ModelDocumentation,
            requestPayload: PayloadSchema,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            responseHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                requestPayload.Request().schema(),
                requestPayload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                documentation.payload.Response().schema(),
                documentation.payload.Response().getSchemaFieldDescriptors(),
                responseHeaders,
                documentRequestPayload,
                hasRequestPayload
            )
        }

        fun ResultActions.andDocumentErrorResponse(
            documentation: ModelDocumentation,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                documentation.payload.Request().schema(),
                documentation.payload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                errorPayloadSchema.Response().schema(),
                errorPayloadSchema.Response().getSchemaFieldDescriptors(),
                emptyList(),
                documentRequestPayload,
                hasRequestPayload
            )
        }

        fun ResultActions.andDocumentCustomRequestSchemaErrorResponse(
            documentation: ModelDocumentation,
            requestPayload: PayloadSchema,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                requestPayload.Request().schema(),
                requestPayload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                errorPayloadSchema.Response().schema(),
                errorPayloadSchema.Response().getSchemaFieldDescriptors(),
                emptyList(),
                documentRequestPayload,
                hasRequestPayload
            )
        }

        fun ResultActions.andDocumentEmptyObjectResponse(
            documentation: ModelDocumentation,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                documentation.payload.Request().schema(),
                documentation.payload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                emptyPayload.Response().schema(),
                emptyPayload.Response().getSchemaFieldDescriptors(),
                emptyList(),
                documentRequestPayload,
                hasRequestPayload
            )
        }

        fun ResultActions.andDocumentCustomRequestSchemaEmptyResponse(
            documentation: ModelDocumentation,
            requestPayload: PayloadSchema,
            summary: String? = null,
            description: String? = null,
            requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
            urlParameters: List<ParameterDescriptorWithType> = emptyList(),
            documentRequestPayload: Boolean = false,
            hasRequestPayload: Boolean = false
        ): ResultActions {
            return documenter(
                documentation,
                summary,
                description,
                urlParameters,
                requestPayload.Request().schema(),
                requestPayload.Request().getSchemaFieldDescriptors(),
                requestHeaders,
                emptyPayload.Response().schema(),
                emptyPayload.Response().getSchemaFieldDescriptors(),
                emptyList(),
                documentRequestPayload,
                hasRequestPayload
            )
        }

        private fun ResultActions.documenter(
            documentation: ModelDocumentation,
            summary: String?,
            description: String?,
            urlParameters: List<ParameterDescriptorWithType>,
            requestSchema: Schema,
            requestFields: List<FieldDescriptor>,
            requestHeaders: List<HeaderDescriptorWithType>,
            responseSchema: Schema,
            responseFields: List<FieldDescriptor>,
            responseHeaders: List<HeaderDescriptorWithType>,
            documentRequestPayload: Boolean,
            hasRequestPayload: Boolean
        ): ResultActions {
            val builder = builder()
            if (summary != null) {
                builder.summary(summary)
            }

            if (description != null) {
                builder.description(description)
            }

            if (hasRequestPayload || documentRequestPayload) {
                builder.requestSchema(requestSchema)
            }

            if (requestFields.isNotEmpty() && documentRequestPayload) {
                builder.requestFields(requestFields)
            }

            builder.requestHeaders(requestHeaders).pathParameters(urlParameters)

            builder.responseSchema(responseSchema).responseFields(responseFields).responseHeaders(responseHeaders)

            builder.tag(documentation.tag.fullName)

            this.andDo(
                document(
                    "${documentation.tag.name}/{ClassName}/{methodName}",
                    snippets = arrayOf(
                        ResourceDocumentation.resource(builder.build())
                    )
                )
            )

            return this
        }
    }
}
