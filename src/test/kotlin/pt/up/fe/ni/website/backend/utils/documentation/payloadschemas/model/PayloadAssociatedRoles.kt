package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema

class PayloadAssociatedRoles {
    companion object {
        val payload = PayloadSchema(
            "associated-role",
            mutableListOf(
                DocumentedJSONField("name", "Name of the role", JsonFieldType.STRING),
                DocumentedJSONField("permissions", "Array of permissions", JsonFieldType.ARRAY),
                DocumentedJSONField("permissions[].*", "Permission", JsonFieldType.NUMBER, isInResponse = false),
                DocumentedJSONField("permissions[].*", "Permission", JsonFieldType.STRING, isInRequest = false),
                DocumentedJSONField(
                    "id",
                    "Id of the role/activity association",
                    JsonFieldType.NUMBER,
                    isInRequest = false
                ),

                DocumentedJSONField(
                    "isSection",
                    "If the role represents a generation section",
                    JsonFieldType.BOOLEAN
                ),
                DocumentedJSONField(
                    "accountIds",
                    "Array of account ids associated with this role",
                    JsonFieldType.ARRAY,
                    isInResponse = false
                ),
                DocumentedJSONField(
                    "accountIds[].*",
                    "Account id",
                    JsonFieldType.NUMBER,
                    isInResponse = false
                ),
                DocumentedJSONField(
                    "associatedActivities[]",
                    "Array of activities associated with this role",
                    JsonFieldType.ARRAY
                ),
                DocumentedJSONField(
                    "associatedActivities[].activityId",
                    "Id of the activity",
                    JsonFieldType.NUMBER,
                    optional = true,
                    isInResponse = false
                ),
                DocumentedJSONField(
                    "associatedActivities[].permissions",
                    "Permissions of the role in the activity",
                    JsonFieldType.ARRAY
                ),
                DocumentedJSONField(
                    "associatedActivities[].permissions[].*",
                    "Permissions",
                    JsonFieldType.STRING,
                    isInRequest = false
                ),
                DocumentedJSONField(
                    "associatedActivities[].permissions[].*",
                    "Permissions",
                    JsonFieldType.NUMBER,
                    isInResponse = false
                )
            ).addFieldsBeneathPath(
                "associatedActivities[]",
                PayloadRole.payload.documentedJSONFields,
                addResponse = true,
                optional = true
            )
        )
    }

    private class PayloadRole {
        companion object {
            val payload = PayloadSchema(
                "role",
                mutableListOf(
                    DocumentedJSONField("id", "Id of the role", JsonFieldType.NUMBER, isInRequest = false),
                    DocumentedJSONField(
                        "activity",
                        "Activity of the association",
                        JsonFieldType.OBJECT,
                        isInRequest = false
                    )
                ).addFieldsBeneathPath(
                    "activity",
                    PayloadActivity.payload.documentedJSONFields,
                    addResponse = true
                )
            )
        }
    }
}
