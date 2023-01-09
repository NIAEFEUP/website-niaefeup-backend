package pt.up.fe.ni.website.backend.permissions

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PermissionsConverter : AttributeConverter<Permissions, Long> {
    override fun convertToDatabaseColumn(attribute: Permissions) = attribute.toLong()
    override fun convertToEntityAttribute(dbData: Long) = Permissions.fromLong(dbData)
}
