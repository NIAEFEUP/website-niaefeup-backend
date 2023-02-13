package pt.up.fe.ni.website.backend.utils.documentation

import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

class PayloadSchema(
    private val schemaName: String,
    private val documentedJsonFields: List<FieldDescriptor>,
    private val idDescription: String?,
) {
    inner class Request {
        fun schema(): Schema {
            return Schema("$schemaName-request")
        }

        fun documentedFields(): MutableList<FieldDescriptor> {
            return documentedJsonFields.toMutableList()
        }
    }

    inner class Response {
        fun schema(): Schema {
            return Schema("$schemaName-response")
        }

        fun documentedFields(): MutableList<FieldDescriptor> {
            val fieldsList = documentedJsonFields.toMutableList()
            idDescription?.let {
                fieldsList.add(
                    PayloadDocumentation.fieldWithPath("id").type(JsonFieldType.NUMBER).description(it),
                )
            }
            return fieldsList
        }

        fun arraySchema(): Schema {
            return Schema("$schemaName-response-array")
        }

        fun arrayDocumentedFields(): MutableList<FieldDescriptor> {
            val fieldsList = documentedJsonFields.toMutableList()
            idDescription?.let {
                fieldsList.add(
                    PayloadDocumentation.fieldWithPath("id").type(JsonFieldType.NUMBER).description(it),
                )
            }

            val arrayFieldsList = mutableListOf<FieldDescriptor>()
            for (field in fieldsList) {
                val arrayField = PayloadDocumentation.fieldWithPath("[].${field.path}").type(field.type)
                    .description(field.description)

                if (field.isOptional) {
                    arrayField.optional()
                }
                if (field.isIgnored) {
                    arrayField.ignored()
                }

                arrayFieldsList.add(arrayField)
            }

            return arrayFieldsList
        }
    }
}
