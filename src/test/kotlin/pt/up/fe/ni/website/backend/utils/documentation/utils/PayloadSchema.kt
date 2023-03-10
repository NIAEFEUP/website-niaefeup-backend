package pt.up.fe.ni.website.backend.utils.documentation.utils

import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.payload.FieldDescriptor
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath

open class PayloadSchema(
    open val schemaName: String,
    val documentedJSONFields: MutableList<DocumentedJSONField>
) {
    companion object {
        enum class MessageType(val type: String) {
            REQUEST("request"), RESPONSE("response")
        }
    }

    open fun getPayloadArraySchema(): PayloadSchema {
        val newPayload = PayloadSchema("arrayOf-$schemaName", mutableListOf())
        newPayload.addBeneathPath("[]", documentedJSONFields)
        return newPayload
    }

    abstract inner class Message(private val type: MessageType) {
        fun schema(): Schema {
            return Schema("$schemaName-${type.type}")
        }

        fun getSchemaFieldDescriptors(): MutableList<FieldDescriptor> {
            val fields = mutableListOf<FieldDescriptor>()

            documentedJSONFields.forEach { field ->
                if ((field.isInRequest && type == MessageType.REQUEST) ||
                    (field.isInResponse && type == MessageType.RESPONSE)
                ) {
                    fields.add(field.getFieldDescriptor())
                }
            }

            return fields
        }
    }

    inner class Request : Message(MessageType.REQUEST)

    inner class Response : Message(MessageType.RESPONSE)

    private fun addBeneathPath(
        path: String,
        documentedJSONFields: MutableList<DocumentedJSONField>
    ) {
        this.documentedJSONFields.addFieldsBeneathPath(
            path,
            documentedJSONFields,
            addRequest = true,
            addResponse = true
        )
    }
}
