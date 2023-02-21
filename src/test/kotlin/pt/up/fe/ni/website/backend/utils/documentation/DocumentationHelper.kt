package pt.up.fe.ni.website.backend.utils.documentation

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

class DocumentationHelper {
    companion object {
        fun MutableList<FieldDescriptor>.addFieldsToPayloadBeneathPath(
            path: String,
            documentedJsonFields: List<FieldDescriptor>,
            optional: Boolean = false,
            ignored: Boolean = false,
        ): MutableList<FieldDescriptor> {
            documentedJsonFields.forEach {
                val field = fieldWithPath(
                    path +
                        (if (it.path.startsWith("[") || it.path.startsWith(".")) "" else ".") + it.path,
                )
                    .type(it.type).description(it.description)

                if (it.isOptional || optional) {
                    field.optional()
                }

                if (it.isIgnored || ignored) {
                    field.ignored()
                }

                field.attributes.putAll(it.attributes)

                this.add(field)
            }

            return this
        }
    }
}
