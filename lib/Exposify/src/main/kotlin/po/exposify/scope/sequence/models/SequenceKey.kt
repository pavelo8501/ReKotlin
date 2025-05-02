package po.exposify.scope.sequence.models

data class SequenceKey(
    val dtoClassName: String,
    val sequenceId : Int
)
{

    override fun equals(other: Any?): Boolean {
        return other is SequenceKey &&
                this.dtoClassName == other.dtoClassName &&
                this.sequenceId == other.sequenceId
    }

    override fun hashCode(): Int {
        return 31 * dtoClassName.hashCode() + sequenceId
    }

    override fun toString(): String = "${dtoClassName}#$sequenceId"

}