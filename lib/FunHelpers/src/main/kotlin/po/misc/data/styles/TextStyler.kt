package po.misc.data.styles

import po.misc.context.CTX
import po.misc.data.MetaProvider
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.TextContaining
import po.misc.data.strings.FormattedText
import po.misc.data.styles.Colour.RESET
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import kotlin.reflect.KClass


data class AnsiColorSegment(
    val start: Int,    // index of color code
    val end: Int,      // index where it is reset
    override val code: String   // the actual ANSI start code like \u001B[31m
): StyleCode{ override val ordinal: Int = 1}

open class StringFormatter{

    val ansiColourRegex: Regex = Regex(ANSI_COLOR_PATTERN)
    val ansiStartRegex: Regex = Regex(ANSI_START_PATTERN)
    val ansiResetRegex: Regex = Regex(ANSI_RESET_PATTERN)
    val ansiRegex: Regex = Regex(ANSI_PATTERN)

    protected fun stripAnsiIfAny(text:String):String {
        if(isTextStyled(text)){
            return  stripAnsi(text)
        }
        return text
    }
    internal fun stripAnsi(text: String): String = text.replace(ansiRegex, "")

    protected fun isTextStyled(text:String):Boolean {
        return text.contains(RESET)
    }
    protected fun extractColorSegments(text: String): List<AnsiColorSegment> {
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
        return "${styleCode.code}$stripped${TextStyle.Reset.code}"
    }

    fun colour(text: String, colour: Colour): String  =  overwriteStyle(text, colour)
    fun style(text: String, style: StyleCode?): String  =  overwriteStyle(text, style?:TextStyle.Regular)

    fun style(
        text: String,
        color: Colour,
        background: BGColour
    ): String = overwriteStyle(text, StyleTheme(color, background))

    fun applyStyle(
        text: String,
        color: Colour,
    ): String = applyStyleCode(text, color)

    fun formatKnownTypes(receiver: Any?): FormattedText {
        return if(receiver != null){
            val targetAsString = receiver.toString()
            when(receiver){
                is KClass<*> -> {
                    val info = ClassResolver.classInfo(receiver)
                    FormattedText(info.simpleName, info.formattedClassName)
                }
                is MetaProvider ->{
                    FormattedText(receiver.metaText)
                }
                is PrettyFormatted -> {
                    FormattedText(targetAsString).also {
                        it.overflowPrevention = true
                    }
                }
                is PrettyPrint -> {
                    FormattedText(targetAsString, receiver.formattedString)
                }
                is CTX -> FormattedText(targetAsString,  receiver.identifiedByName)
                is Enum<*> -> {
                    if(receiver is TextContaining){
                        FormattedText(targetAsString, "${receiver.name}: ${receiver.asText()}")
                    }else{
                        FormattedText(targetAsString)
                    }
                }
                is Throwable ->{
                    FormattedText(receiver.message?:"", receiver.throwableToText())
                }
                is String -> FormattedText(targetAsString)
                is Boolean -> {
                    if(receiver){
                        FormattedText("true",  "True".colorize(Colour.Green))
                    }else{
                        FormattedText("false",  "False".colorize(Colour.Red))
                    }
                }
                else -> FormattedText(targetAsString)
            }
        }else{
            FormattedText("null", "null".colorize(Colour.Yellow))
        }
    }

    companion object{
        const val ANSI_COLOR_PATTERN: String = "\\u001B\\[(?!0m)[0-9;]*m"
        const val ANSI_START_PATTERN: String = "\u001B\\[(?!0m)[0-9;]*m"
        const val ANSI_RESET_PATTERN: String = "\u001B\\[0m"
        const val ANSI_PATTERN :String = "\\u001B\\[[;\\d]*m"
    }
}

interface TextStyler{

    val String.isStyled: Boolean get() = isTextStyled(this)

    fun String.style(styleCode: StyleCode): String = overwriteStyle(this, styleCode)
    fun style(text: String, style: TextStyle, color: Colour): String = overwriteStyle(text,  StyleTheme(style, color, BGColour.Default))

    fun style(
        text: String,
        style: TextStyle,
        color: Colour,
        background: BGColour
    ): String = overwriteStyle(text,  StyleTheme(style, color, background))

    fun applyStyle(
        text: String,
        style: TextStyle,
        color: Colour,
        background: BGColour
    ): String = applyStyleCode(text,  StyleTheme(style, color, background))

    fun String.stripAnsi(): String = stripAnsi(this)
    fun String.extractStyleSegments(): List<AnsiColorSegment> = extractColorSegments(this)
    fun String.applyStyleSegment(colourSegment:  AnsiColorSegment): String = overwriteStyle(this,  colourSegment)

    companion object: StringFormatter()
}