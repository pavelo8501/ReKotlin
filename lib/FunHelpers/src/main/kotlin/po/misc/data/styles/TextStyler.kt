package po.misc.data.styles

import po.misc.data.styles.Colorizer.Companion.colour
import po.misc.data.styles.Colorizer.Companion.isStyled
import po.misc.data.styles.Colour.RESET
import kotlin.text.contains

open class StyleHelper{

    data class AnsiColorSegment(
        val start: Int,    // index of color code
        val end: Int,      // index where it is reset
        val code: String   // the actual ANSI start code like \u001B[31m
    )

    val ANSI_COLOR_REGEX: Regex = Regex("\\u001B\\[(?!0m)[0-9;]*m")
    val ANSI_START_REGEX: Regex = Regex("\u001B\\[(?!0m)[0-9;]*m")
    val ANSI_RESET_REGEX: Regex = Regex("\u001B\\[0m")

    private val ANSI_REGEX = Regex("\\u001B\\[[;\\d]*m")

    protected fun stripAnsiIfAny(text:String):String {
        if(isTextStyled(text)){
            return  stripAnsi(text)
        }
        return text
    }
    protected fun stripAnsi(text: String): String = text.replace(ANSI_REGEX, "")

    protected  fun isTextStyled(text:String):Boolean {
        return text.contains(RESET.code)
    }

    protected fun extractColorSegments(text: String): List<AnsiColorSegment> {
        val segments = mutableListOf<AnsiColorSegment>()
        val starts = ANSI_START_REGEX.findAll(text).toList()
        val resets = ANSI_RESET_REGEX.findAll(text).toList()
        for (startMatch in starts) {
            val resetMatch = resets.firstOrNull { it.range.first > startMatch.range.first }
            if (resetMatch != null) {
                segments.add(
                    AnsiColorSegment(
                        start = startMatch.range.first,
                        end = resetMatch.range.last + 1,
                        code = startMatch.value
                    )
                )
            } else {
                segments.add(
                    AnsiColorSegment(
                        start = startMatch.range.first,
                        end = text.length,
                        code = startMatch.value
                    )
                )
            }
        }
        return segments
    }


    /**
     * Applies the given [colour] **only to unstyled text segments** while
     * preserving all existing ANSI-styled regions intact.
     *
     * ### Safety-first behavior
     * This function is intentionally **non-destructive**:
     * - Already colorized (ANSI-styled) segments are **never modified**
     * - Only plain (unstyled) gaps between styled segments are recolored
     *
     * This prevents accidental overwriting, nesting, or corruption of
     * previously applied terminal styles.
     *
     * ### Why this exists
     * Reapplying colour blindly to a partially styled string can:
     * - Break existing colour boundaries
     * - Introduce invalid RESET sequences
     * - Produce unreadable or inconsistent terminal output
     *
     * This function avoids those issues by:
     * 1. Detecting existing ANSI colour segments
     * 2. Preserving them verbatim
     * 3. Re-colouring only the unstyled parts
     *
     * ### Important
     * This is **not** a "force recolour" function.
     * If you need to override all colours, use colour(text: String, colour: Colour)
     *
     * @param text the input string which may contain ANSI-styled segments
     * @param colour the colour to apply to unstyled segments only
     * @return a safely colourized string with existing styles preserved
     */
    fun applyStyleCode(text: String, styleCode: StyleCode): String {
        val segments = extractColorSegments(text)
        val result = StringBuilder()
        var lastIndex = 0
        segments.forEach { segment ->
            if (segment.start > lastIndex) {
                val before = text.substring(lastIndex, segment.start)
                if(!isTextStyled(before)){
                    val stripped = stripAnsi(before)
                    val reColorized = colour(stripped, styleCode)
                    result.append(reColorized)
                }
            }
            val subtext = text.substring(segment.start, segment.end)
            result.append(subtext)
            lastIndex = segment.end
        }
        if (lastIndex < text.length) {
            val tail = text.substring(lastIndex)
            if(!isTextStyled(tail)){
                val strippedTail = stripAnsi(tail)
                val reColorizedTail = colour(strippedTail, styleCode)
                result.append(reColorizedTail)
            }
        }
        return result.toString()
    }
    fun overwriteStyle(text: String, styleCode: StyleCode):String{
        return "${styleCode.code}$text${TextStyle.Reset.code}"
    }
}

interface TextStyler{

    fun isStyled(text:String):Boolean = isTextStyled(text)

    fun style(text: String, style: StyleCode?): String  = overwriteStyle(text, style?:TextStyle.Regular)

    fun style(text: String, style: TextStyle, color: Colour): String = overwriteStyle(text,  StyleTheme(style, color, BGColour.Default))
    fun style(text: String, style: TextStyle, color: Colour, background: BGColour): String =
        overwriteStyle(text,  StyleTheme(style, color, background))

    fun applyStyle(text: String, style: TextStyle, color: Colour, background: BGColour): String =
        applyStyleCode(text,  StyleTheme(style, color, background))


    companion object: StyleHelper()

}