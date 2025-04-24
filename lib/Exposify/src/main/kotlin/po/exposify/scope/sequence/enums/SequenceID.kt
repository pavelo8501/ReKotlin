package po.exposify.scope.sequence.enums

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

enum class SequenceID(val value: Int) {
    DEFAULT(0),
    SELECT(1),
    UPDATE(2),
    PICK(3);

    companion object {
        private val VALUES = entries.toTypedArray()

        operator fun getValue(value : SequenceID, property: KProperty<*>): Int{
            return asValue(value)
        }

        fun sequenceID(value: Int): SequenceID = VALUES.firstOrNull { it.value == value }?: DEFAULT

        fun asValue(sequenceId: SequenceID): Int {
            return sequenceId.value
        }

    }
}