package po.exposify.dto.models

import po.misc.reflection.mappers.models.PropertyContainer

class CheckedProperty(
    val propertyRecord: PropertyContainer<*>,
    var checked: Boolean = false
) {
}