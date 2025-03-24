package po.exposify.scope.sequence

enum class SequenceID(val value: Int) {
    DEFAULT(0),
    SELECT(1),
    UPDATE(2),
    PICK(3);

    companion object {
        private val VALUES = SequenceID.entries.toTypedArray()
        fun value(value: Int): SequenceID = VALUES.firstOrNull { it.value == value }?: DEFAULT
    }
}