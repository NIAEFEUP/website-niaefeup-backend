package pt.up.fe.ni.website.backend.utils.documentation

import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation

open class PayloadSchema(
    private val schemaName: String,
    val documentedJsonFields: List<FieldDescriptor>,
) {
    inner class Request {
        fun schema(): Schema {
            return Schema("$schemaName-request")
        }

        fun documentedFields(requestOnlyFields: List<FieldDescriptor>? = null): MutableList<FieldDescriptor> {
            requestOnlyFields?.let {
                val requestFields = documentedJsonFields.toMutableList()
                requestFields.addAll(it)
                return requestFields
            }
            return documentedJsonFields.toMutableList()
        }
    }

    inner class Response {
        fun schema(): Schema {
            return Schema("$schemaName-response")
        }

        fun documentedFields(responseOnlyFields: List<FieldDescriptor>? = null): MutableList<FieldDescriptor> {
            val fieldsList = documentedJsonFields.toMutableList()
            responseOnlyFields?.let {
                fieldsList.addAll(it)
            }
            return fieldsList
        }

        fun arraySchema(): Schema {
            return Schema("$schemaName-response-array")
        }

        fun arrayDocumentedFields(responseOnlyFields: List<FieldDescriptor>? = null): MutableList<FieldDescriptor> {
            val fieldsList = documentedJsonFields.toMutableList()
            responseOnlyFields?.let { fieldsList.addAll(it) }

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
