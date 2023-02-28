package pt.up.fe.ni.website.backend.utils.documentation

import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

class DocumentedJSONField(
    var path: String,
    val description: String? = null,
    private val type: JsonFieldType? = null,
    var optional: Boolean = false,
    var ignored: Boolean = false,
    private val attributes: Map<String, JvmType.Object> = emptyMap(),
    var isInRequest: Boolean = true,
    var isInResponse: Boolean = true
) : Cloneable {
    companion object {
        fun MutableList<DocumentedJSONField>.addFieldsBeneathPath(
            path: String,
            documentedJSONFields: List<DocumentedJSONField>,
            optional: Boolean = false,
            ignored: Boolean = false,
            addRequest: Boolean = false,
            addResponse: Boolean = false
        ): MutableList<DocumentedJSONField> {
            documentedJSONFields.forEach { field ->
                val fieldBeneath = field.clone() as DocumentedJSONField
                fieldBeneath.path = path +
                    (if (field.path.startsWith("[") || field.path.startsWith(".")) "" else ".") + field.path

                fieldBeneath.optional = field.optional || optional
                fieldBeneath.ignored = field.ignored || ignored
                fieldBeneath.isInRequest = addRequest && field.isInRequest
                fieldBeneath.isInResponse = addResponse && field.isInResponse

                this.add(fieldBeneath)
            }
            return this
        }
    }

    fun getFieldDescriptor(): FieldDescriptor {
        val fieldDescriptor = fieldWithPath(path)
        description?.let { fieldDescriptor.description(it) }
        type?.let { fieldDescriptor.type(it) }
        if (optional) fieldDescriptor.optional()
        if (ignored) fieldDescriptor.ignored()
        fieldDescriptor.attributes.putAll(attributes)

        return fieldDescriptor
    }

    public override fun clone(): Any {
        return super.clone()
    }
}
