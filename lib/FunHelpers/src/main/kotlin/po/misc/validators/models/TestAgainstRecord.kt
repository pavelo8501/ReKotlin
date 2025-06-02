package po.misc.validators.models

import po.misc.interfaces.Identifiable
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.types.TypeRecord

class ValidationInstance<T: Any>(
    val component: Identifiable,
    val source: TypeRecord<T>,
    var records: List<InstanceRecord<T>> = emptyList()
)


open class ValidationClass<T: Any>(
    val component: Identifiable,
    val source: TypeRecord<T>,
    var records: List<ValidationRecord> = emptyList()
)

data class InstanceRecord<T: Any>(
    val instance: T,
    val propertyRecord: PropertyRecord<*>?,
)

data class ValidationRecord(
    val propertyRecord: PropertyRecord<*>,
)
