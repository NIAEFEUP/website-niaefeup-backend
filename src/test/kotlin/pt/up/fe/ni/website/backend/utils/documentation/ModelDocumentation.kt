package pt.up.fe.ni.website.backend.utils.documentation

open class ModelDocumentation(
    schemaName: String,
    val tag: Tag,
    documentedJsonFields: MutableList<DocumentedJSONField>
) {
    val payload = PayloadSchema(schemaName, documentedJsonFields)

    companion object {
        enum class Tag(val fullName: String) {
            AUTH("Authentication"),
            ACCOUNT("Accounts"),
            EVENT("Events"),
            POST("Posts"),
            PROJECT("Projects")
        }
    }

    fun getModelDocumentationArray(): ModelDocumentation {
        val payloadArraySchema = payload.getPayloadArraySchema()
        return ModelDocumentation(payloadArraySchema.schemaName, tag, payloadArraySchema.documentedJSONFields)
    }
}
