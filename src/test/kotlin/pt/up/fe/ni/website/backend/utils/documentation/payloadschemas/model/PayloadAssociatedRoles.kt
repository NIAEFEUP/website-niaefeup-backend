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
                DocumentedJSONField("permissions", "Array of permissions", JsonFieldType.ARRAY),
                DocumentedJSONField("permissions[].*", "Permission", JsonFieldType.STRING),
                DocumentedJSONField("id", "Id of the role/activity association", JsonFieldType.NUMBER),
                DocumentedJSONField("name", "Name of the role", JsonFieldType.STRING),
                DocumentedJSONField(
                    "isSection",
                    "If the role represents a generation section",
                    JsonFieldType.BOOLEAN
                ),
                DocumentedJSONField(
                    "accountIds",
                    "Array of account ids associated with this role",
                    JsonFieldType.ARRAY
                ),
                DocumentedJSONField(
                    "associatedActivities[]",
                    "Array of ids of activities associated with this role",
                    JsonFieldType.ARRAY
                ),
                DocumentedJSONField(
                    "associatedActivities[].id",
                    "Id of the role",
                    JsonFieldType.NUMBER
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
                    JsonFieldType.ARRAY,
                    optional = true
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
                    DocumentedJSONField("id", "Id of the role", JsonFieldType.NUMBER),
                    DocumentedJSONField("activity", "Activity of the association", JsonFieldType.OBJECT)
                ).addFieldsBeneathPath(
                    "activity",
                    PayloadActivity.payload.documentedJSONFields,
                    addRequest = true,
                    addResponse = true
                )
            )
        }
    }
}
