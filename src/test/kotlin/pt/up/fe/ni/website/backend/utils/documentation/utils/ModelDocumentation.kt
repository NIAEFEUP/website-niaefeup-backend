package pt.up.fe.ni.website.backend.utils.documentation.utils

open class ModelDocumentation(
    schemaName: String,
    val tag: ITag,
    documentedJsonFields: MutableList<DocumentedJSONField>
) {
    val payload = PayloadSchema(schemaName, documentedJsonFields)

    fun getModelDocumentationArray(): ModelDocumentation {
        val payloadArraySchema = payload.getPayloadArraySchema()
        return ModelDocumentation(payloadArraySchema.schemaName, tag, payloadArraySchema.documentedJSONFields)
    }
}
