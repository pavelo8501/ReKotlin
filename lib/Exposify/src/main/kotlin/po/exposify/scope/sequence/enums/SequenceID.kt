package po.exposify.scope.sequence.enums

enum class SequenceID(val value: Int) {
    DEFAULT(0),
    SELECT(1),
    UPDATE(2),
    PICK(3);

    companion object {
        private val VALUES = entries.toTypedArray()

        fun sequenceID(value: Int): SequenceID = VALUES.firstOrNull { it.value == value }?: DEFAULT

        fun value(sequenceId: SequenceID): Int {
            return sequenceId.value
        }

    }
}