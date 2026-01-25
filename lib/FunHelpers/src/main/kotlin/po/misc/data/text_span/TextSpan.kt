package po.misc.data.text_span

import po.misc.data.Styled
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler


/**
 * ImmutableMutable text container.
 *
 * Produce new instances.
 */
interface TextSpan{
    val plain: String
    val styled: String
    val plainLength: Int get() = plain.length
    val styledLength: Int get() = styled.length
    val hasLineBreak: Boolean get() = plain.contains(SpecialChars.NEW_LINE)
    val endsLineBreak: Boolean get() {
        if(plain.isEmpty()) return false
        val lastChar = plain.takeLast(1)
        return lastChar == SpecialChars.NEW_LINE
    }

    fun copy(): StyledPair = StyledPair(this)
    fun copyAsStacked(): MutablePair = MutablePair(this)

}

class StyledPair(
    override val plain: String= "",
    override val styled: String = plain
): TextSpan {

    constructor(textSpan: TextSpan):this(textSpan.plain, textSpan.styled)

    override val plainLength: Int = plain.length
    override val styledLength: Int = styled.length
    override val hasLineBreak: Boolean = plain.contains(SpecialChars.NEW_LINE)

    operator fun plus(other: TextSpan): TextSpan = append(other)
    
    override fun equals(other: Any?): Boolean {
       return when(other) {
            is TextSpan -> styled == other.styled
            is String -> styled == other
            else -> false
        }
    }
    override fun hashCode(): Int = styled.hashCode()
    override fun toString(): String = plain
}

infix fun TextSpan.prepend(text: String):TextSpan{
   val strippedPlain = TextStyler.stripAnsi(text)
   return StyledPair(strippedPlain + this.plain, text+ this.styled)
}
infix fun TextSpan.prepend(other: TextSpan):TextSpan{
   return StyledPair(other.plain + this.plain, other.styled + this.styled)
}

infix fun TextSpan.append(text: String):TextSpan{
    val strippedPlain = TextStyler.stripAnsi(text)
    return StyledPair(
        this.plain + strippedPlain,
        this.styled + text
    )
}
infix fun TextSpan.append(other: TextSpan):TextSpan{
    return StyledPair(
        this.plain + other.plain,
        this.styled + other.styled
    )
}

fun List<TextSpan>.joinSpans(orientation: Orientation): TextSpan{
    val builder =  SpanBuilder()
    val lastSpanIndex = lastIndex
    if (orientation == Orientation.Horizontal) {
        forEach {
            builder.append(it)
        }
    } else {
        forEachIndexed { index, span ->
            val isLast = index == lastSpanIndex
            if (!isLast){
                if(span.endsLineBreak){
                    builder.append(span)
                }else{
                    builder.appendLine(span)
                }
            }else{
                if(span.endsLineBreak){
                    builder.append(span)
                }else{
                    builder.append(span)
                }
            }
        }
    }
    val result = builder.toPair()
    return result
}