package po.misc.data.styles

import po.misc.data.strings.contains
import po.misc.data.styles.Colour.RESET
import po.misc.types.k_class.simpleOrAnon
import java.awt.Color


open class AnsiStyler {

    internal fun stripAnsi(text: String): String {
        if(isTextStyled(text)){
            return text.replace(ansiRegex, "")
        }
        return text
    }

    internal fun tryTranslateJavaColor(color: Color): StyleCode{
        return Colour.matchByNameOrDefault(color::class.simpleOrAnon)
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
    internal fun applyStyleCode(text: String, styleCode: StyleCode): String {
        val segments = extractColorSegments(text)
        val result = StringBuilder()
        var lastIndex = 0
        segments.forEach { segment ->
            if (segment.start > lastIndex) {
                val before = text.substring(lastIndex, segment.start)
                if(!isTextStyled(before)){
                    val stripped = stripAnsi(before)
                    val reColorized = style(stripped, styleCode)
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
                val reColorizedTail = style(strippedTail, styleCode)
                result.append(reColorizedTail)
            }
        }
        return result.toString()
    }
    internal fun overwriteStyle(text: String, styleCode: StyleCode):String{
        if(styleCode.ordinal == 0) {
            return text
        }
        val stripped = stripAnsi(text)
        return buildString(stripped.length + 16) {
            append(styleCode.code)
            for (i in stripped.indices) {
                val char = stripped[i]
                if (char == '\n') {
                    append(RESET.code); append('\n'); append(styleCode.code)
                }else {
                    append(char)
                }
            }
            append(RESET.code)
        }
    }

    fun isTextStyled(text:String):Boolean {
        return text.contains(RESET)
    }
    fun extractColorSegments(text: String): List<AnsiColorSegment> {
        val segments = mutableListOf<AnsiColorSegment>()
        val starts = ansiStartRegex.findAll(text).toList()
        val resets = ansiResetRegex.findAll(text).toList()
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

    fun colour(text: String, colour: Colour): String  =  overwriteStyle(text, colour)
    fun style(text: String, style: StyleCode): String = overwriteStyle(text, style)
    fun style(
        text: String,
        color: Colour,
        background: BGColour
    ): String = overwriteStyle(text, StyleTheme(color, background))

    fun applyStyle(
        text: String,
        style: StyleCode
    ): String = applyStyleCode(text, style)

    companion object{

        const val ANSI_COLOR_PATTERN: String = "\\u001B\\[(?!0m)[0-9;]*m"
        const val ANSI_START_PATTERN: String = "\u001B\\[(?!0m)[0-9;]*m"
        const val ANSI_RESET_PATTERN: String = "\u001B\\[0m"
        const val ANSI_PATTERN :String = "\\u001B\\[[;\\d]*m"
        const val LINE_BREAK_PATTERN: String = "\\r\\n|\\r|\\n"

        val ansiColourRegex: Regex = Regex(ANSI_COLOR_PATTERN)
        val ansiStartRegex: Regex = Regex(ANSI_START_PATTERN)
        val ansiResetRegex: Regex = Regex(ANSI_RESET_PATTERN)
        val ansiRegex: Regex = Regex(ANSI_PATTERN)
        val lineBreakRegex: Regex = Regex(LINE_BREAK_PATTERN)
    }
}