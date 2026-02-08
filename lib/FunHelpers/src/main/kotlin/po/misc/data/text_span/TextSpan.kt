package po.misc.data.text_span

import po.misc.data.StringParameters
import po.misc.data.styles.SpecialChars
import po.misc.data.takeLastOrNull

interface SpanRole


/**
 * ImmutableMutable text container.
 *
 * Produce new instances.
 */
interface TextSpan:  StringParameters{
    val plain: String
    val styled: String
    val role: SpanRole? get() = null
    override val plainLength: Int get() = plain.length
    override val hasLineBreak: Boolean get() = plain.contains(SpecialChars.NEW_LINE)
    override val endsLineBreak: Boolean get() {
        val lastChar = plain.takeLastOrNull(1) ?: return false
        return lastChar == SpecialChars.NEW_LINE
    }

    fun copy(newRole: SpanRole? = null): TextSpan
}

class StyledPair(
    override val plain: String = "",
    override val styled: String = plain,
    override val role: SpanRole? = null
): TextSpan {

    constructor(textSpan: TextSpan, newRole: SpanRole? = null):this(textSpan.plain, textSpan.styled, newRole?:textSpan.role)

    operator fun plus(other: TextSpan): TextSpan = appendCreating(other)

    override fun equals(other: Any?): Boolean {
       return when(other) {
            is TextSpan -> styled == other.styled
            is String -> styled == other
            else -> false
        }
    }
    override fun copy(newRole: SpanRole? ):StyledPair{
      return  StyledPair(plain, styled,  newRole?:role)
    }
    override fun hashCode(): Int = styled.hashCode()
    override fun toString(): String = plain
}
