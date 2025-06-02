package po.misc.reflection.properties

import po.misc.reflection.properties.mappers.models.PropertyRecord
import kotlin.reflect.full.memberProperties


inline fun <reified T: Any> toPropertyMap(): Map<String, PropertyRecord<T>>
    = T::class.memberProperties.associate {it.name to PropertyRecord.create(it)}
