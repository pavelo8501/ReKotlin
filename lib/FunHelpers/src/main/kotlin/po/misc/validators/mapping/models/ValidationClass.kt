package po.misc.validators.mapping.models

import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.reflection.mappers.models.PropertyRecord


sealed interface ValidationSubject<T: Any>{
    val component: CTX
    val validatableRecords : List<ValidatableRecord>
}

interface ValidatableRecord{
    val propertyRecord: PropertyRecord<*>
}


class ValidationClass<T: Any>(
    override val component: CTX,
    override var validatableRecords: List<ValidationRecord> = emptyList()
):ValidationSubject<T>{

    fun addRecord(record : ValidationRecord):ValidationClass<T>{
        validatableRecords = validatableRecords.toMutableList().also { it.add(record) }
        return this
    }
}

class ValidationInstance<T: Any>(
    override val component: CTX,
    override var validatableRecords: List<InstanceRecord<T>> = emptyList()
): ValidationSubject<T>{

    fun addRecord(record : InstanceRecord<T>):ValidationInstance<T>{
        validatableRecords = validatableRecords.toMutableList().also { it.add(record) }
        return this
    }
}


data class InstanceRecord<T: Any>(
    val instance: T,
    override val propertyRecord: PropertyRecord<*>,
):ValidatableRecord

data class ValidationRecord(
    override val propertyRecord: PropertyRecord<*>,
):ValidatableRecord
