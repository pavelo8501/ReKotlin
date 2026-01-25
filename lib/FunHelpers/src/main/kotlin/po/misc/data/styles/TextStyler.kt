package po.misc.data.styles

import po.misc.context.CTX
import po.misc.data.MetaProvider
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.Styled
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.strings.contains
import po.misc.data.styles.Colour.RESET
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import po.misc.interfaces.named.TextContaining
import po.misc.reflection.displayName
import po.misc.types.k_class.simpleOrAnon
import java.awt.Color
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0


data class AnsiColorSegment(
    val start: Int,    // index of color code
    val end: Int,      // index where it is reset
    override val code: String   // the actual ANSI start code like \u001B[31m
): StyleCode{
    override val name: String = "AnsiColorSegment"
    override val ordinal: Int = 1
}

class TransformParameters(
    val separator:String = ", ",
    val postfix:String = "",
    val transformAction: ((String) ->  String)? = null
){
    fun applySeparator(text:String):String{
       return text + separator
    }
    fun tryTransform(text:String):String{
       return transformAction?.invoke(text)?:text
    }
}


open class StringFormatter{

    internal fun stripAnsi(text: String): String = text.replace(ansiRegex, "")

    protected fun stripAnsiIfAny(text:String):String {
        if(isTextStyled(text)){
            return  stripAnsi(text)
        }
        return text
    }

    internal fun tryTranslateJavaColor(color: Color): StyleCode{
        return Colour.matchByNameOrDefault(color::class.simpleOrAnon)
    }

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


    fun knownClassFormatting(list: List<*>, separator: String = ", "): TextSpan {
        val mutableSpan = MutablePair()
        list.forEachIndexed { index, any ->
            val isLast = index == list.size - 1
            val span = knownClassFormatting(any)
           if(!isLast){
               mutableSpan.append("${span.plain}$separator", "${span.styled}$separator")
            }else{
               mutableSpan.append(span)
            }
        }
        return mutableSpan
    }

    fun knownClassFormatting(receiver: Any?, transform: (String) ->  String): TextSpan {
          return  when(receiver) {
            is Enum<*> -> {
                val transformed = transform.invoke(receiver.name)
                StyledPair(transformed, transformed.colorize(Colour.Magenta))
            }
            else -> {
                val transformed = transform.invoke(receiver.toString())
                StyledPair(transformed, transformed)
            }
        }
    }

    fun classStyling(receiver: Any?): MutableSpan{
       return when(receiver) {
           is Enum<*> -> {
               val stacked = MutablePair(receiver.name, receiver.name.colorize(Colour.Magenta))
               stacked
           }
           is KProperty0<*> -> {
               val span = knownClassFormatting(receiver.get())
               val plain = receiver.displayName + ": " + span.plain
               val styled = receiver.displayName + ": " + span.styled
               val stacked = MutablePair(plain, styled)
               stacked
           }
           else -> MutablePair(receiver.toString(), receiver.toString())
       }
    }

    private fun receiverToString(
        valuePart: Boolean,
        receiver: Any?
    ):String{
      return  when(receiver) {
            is Enum<*> -> receiver.name
            is KProperty0<*> -> {
                if(valuePart){
                    val result = receiver.get()
                    if(result != null){
                        receiverToString(true,  result)
                    }else{
                        "Null"
                    }
                }else{
                    receiver.displayName
                }
            }
            else -> receiver.toString()
        }
    }

    fun MutableSpan.styleReceiver(
        receiver: Any?,
        parameters: TransformParameters?  = null
    ){
        when(receiver) {
            is Enum<*> -> {
                val result = receiverToString(false,  receiver)
                val transformed = parameters?.tryTransform(result)?:result
                append(transformed, transformed.colorize(Colour.Magenta))
            }
            is KProperty0<*> -> {
                val key  =  receiverToString(false,  receiver)
                val value = receiverToString(true,  receiver)
                val keyValuePair = "$key: $value"
                val styledKeyValuePair = "${key.colorize(Colour.Green)}: ${value.colorize(Colour.Magenta)}"
                val transformedValuePair = parameters?.tryTransform(keyValuePair)?:keyValuePair
                val transformedStyledKeyValuePair = parameters?.tryTransform(styledKeyValuePair)?:styledKeyValuePair
                append(transformedValuePair, transformedStyledKeyValuePair)
            }
            is List<*> -> {
                receiver.forEachIndexed { index, value ->
                    val result =  receiverToString(false,  value)
                    val isLast = index == receiver.lastIndex
                    if(!isLast){
                        parameters?.let {param->
                            append(param.applySeparator(result))
                        }?:run {
                            append(result)
                        }
                    }else{
                        append(result)
                    }
                }
            }
            else -> {
                parameters?.let {param->
                    append(param.tryTransform(receiver.toString()))
                }?:run {
                    append(receiver.toString())
                }
            }
        }
    }


    fun classStyling(receiver: Any?, transform: (MutableSpan) ->  Unit): TextSpan{
         return when(receiver) {
            is Enum<*> -> {
                val stacked = classStyling(receiver)
                transform.invoke(stacked)
                stacked
            }
            is KProperty0<*> ->{
                val stacked = classStyling(receiver)
                transform.invoke(stacked)
                stacked
            }
            else -> {
               val stacked = classStyling(receiver)
                transform.invoke(stacked)
                stacked
            }
        }
    }


    fun knownClassFormatting(receiver: Any?): TextSpan {
       return if(receiver != null){
            when(receiver){
                is List<*>-> knownClassFormatting(receiver)
                is KProperty0<*> ->{
                    val span = knownClassFormatting(receiver.get())
                    val plain = receiver.displayName + ": " + span.plain
                    val styled = receiver.displayName + ": " + span.styled
                    StyledPair(plain, styled)
                }
                is KClass<*> -> StyledPair(receiver.simpleOrAnon)
                is MetaProvider ->  StyledPair(receiver.metaText)
                is TextSpan -> StyledPair(receiver.plain, receiver.styled)
                is Styled -> receiver.textSpan.copy()
                is PrettyPrint ->  StyledPair(stripAnsiIfAny(receiver.formattedString), receiver.formattedString)
                is CTX ->  StyledPair(receiver.identifiedByName)
                is TextContaining -> StyledPair(stripAnsiIfAny(receiver.asText()))
                is Enum<*> -> StyledPair(receiver.name, receiver.name.colorize(Colour.Magenta))
                is Throwable -> {
                    val text = receiver.throwableToText()
                    StyledPair(stripAnsiIfAny(text), text)
                }
                is String -> StyledPair(stripAnsiIfAny(receiver), receiver)
                is Boolean -> {
                    val asText = receiver.toString().uppercase()
                    StyledPair(asText, asText.colorize(Colour.Green))
                }
                else ->{
                    val asText = receiver.toString()
                    StyledPair(stripAnsiIfAny(asText), asText)
                }
            }
        }else{
            StyledPair("Null", "Null".colorize(Colour.Yellow))
        }
    }

    fun formatKnownTypes(receiver: Any?): FormattedText {
        return if(receiver != null){
            val targetAsString = receiver.toString()
            when(receiver){
                is KClass<*> -> {
                    val info = ClassResolver.classInfo(receiver)
                    FormattedText(info.simpleName, info.formattedClassName)
                }
                is MetaProvider -> FormattedText(receiver.metaText)
                is PrettyFormatted -> {
                    FormattedText(targetAsString).also {
                        it.overflowPrevention = true
                    }
                }
                is TextSpan ->{
                    FormattedText(receiver.plain, receiver.styled)
                }
                is Styled ->{
                    FormattedText(receiver.textSpan.plain, receiver.textSpan.styled)
                }
                is PrettyPrint -> FormattedText(targetAsString, receiver.formattedString)
                is CTX -> FormattedText(targetAsString,  receiver.identifiedByName)
                is Enum<*> -> {
                    if(receiver is TextContaining){
                        FormattedText(targetAsString, "${receiver.name}: ${receiver.asText()}")
                    }else{
                        FormattedText(targetAsString)
                    }
                }
                is Throwable -> FormattedText(receiver.message?:"", receiver.throwableToText())
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

    fun stripLineBreaks(text: String): String =
        text.replace(lineBreakRegex, "")

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

interface TextStyler{

    val String.isStyled: Boolean get() = isTextStyled(this)
    val CharSequence.lengthNoAnsi: Int get() = toString().stripAnsi().length

    fun String.style(styleCode: StyleCode): String = overwriteStyle(this, styleCode)
    fun String.style(style: TextStyle, color: Colour): String =
        overwriteStyle(this, StyleTheme(style, color, BGColour.Default))

    fun String.style(style: TextStyle, color: Colour, background: BGColour): String =
        overwriteStyle(this,  StyleTheme(style, color, background))

    fun String.applyStyle(style: TextStyle, color: Colour, background: BGColour): String =
        applyStyleCode(this,  StyleTheme(style, color, background))

    fun String.applyStyle(styleCode: StyleCode, ): String = applyStyleCode(this, styleCode)

    fun String.stripAnsi(): String = stripAnsi(this)
    fun String.extractStyleSegments(): List<AnsiColorSegment> = extractColorSegments(this)
    fun String.applyStyleSegment(colourSegment:  AnsiColorSegment): String = overwriteStyle(this,  colourSegment)

    fun String.toPair(): StyledPair{
        if(!this.isStyled){
            return StyledPair(this)
        }
        return StyledPair(this.stripAnsi(), this)
    }

    fun String.toMutablePair(): MutablePair{
        if(!this.isStyled){
            return MutablePair(this)
        }
        return MutablePair(this.stripAnsi(), this)
    }
    fun Collection<String>.toMutablePairs(): List<MutablePair> = map { it.toMutablePair() }
    fun Array<out String>.toMutablePairs(): List<MutablePair> = map { it.toMutablePair() }

    fun String.stripLineBreaks(): String = stripLineBreaks(this)
    companion object: StringFormatter()
}

fun String.colorize(color: Color): String {
    val colour = TextStyler.tryTranslateJavaColor(color)
   return if(colour == Colour.Default){
        this
    }else{
        TextStyler.style(this, colour)
    }
}
fun String.colorize(colour: Colour): String = TextStyler.style(this, colour)
fun String.colorize(bgColour: BGColour): String = TextStyler.style(this, bgColour)