package po.misc.data.styles

import po.misc.collections.forEachButLast
import po.misc.context.CTX
import po.misc.data.MetaProvider
import po.misc.data.Postfix
import po.misc.data.Prefix
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.Separator
import po.misc.data.Styled
import po.misc.data.TextWrapper
import po.misc.data.strings.StyleRegistry
import po.misc.data.strings.contains
import po.misc.data.styles.Colour.RESET
import po.misc.data.styles.StringFormatter.Companion.lineBreakRegex
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.SpanBuilder
import po.misc.data.text_span.SpanRole
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.asMutable
import po.misc.data.text_span.buildTextSpan
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import po.misc.interfaces.named.NameValue
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



open class StringFormatter{

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

    private fun receiverToString(valuePart: Boolean, receiver: Any?):String{
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
                is Styled -> receiver.textSpan.copy()
                is TextSpan -> StyledPair(receiver.plain, receiver.styled)
                is PrettyPrint ->  StyledPair(stripAnsi(receiver.formattedString), receiver.formattedString)
                is CTX ->  StyledPair(receiver.identifiedByName)
                is TextContaining -> StyledPair(stripAnsi(receiver.asText()))
                is Enum<*> -> StyledPair(receiver.name, receiver.name.colorize(Colour.Magenta))
                is Throwable -> {
                    val text = receiver.throwableToText()
                    StyledPair(stripAnsi(text), text)
                }
                is String -> StyledPair(stripAnsi(receiver), receiver)
                is Boolean -> {
                    val asText = receiver.toString().uppercase()
                    StyledPair(asText, asText.colorize(Colour.Green))
                }
                else ->{
                    val asText = receiver.toString()
                    StyledPair(stripAnsi(asText), asText)
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
                is Styled -> FormattedText(receiver.textSpan.plain, receiver.textSpan.styled)
                is TextSpan -> FormattedText(receiver.plain, receiver.styled)
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

interface TextStyler {

    enum class ValueTag { ClassName, PropertyName, EnumClass, EnumName, Time }
    enum class ActionTag { Info,  Speech }
    enum class TextPart : SpanRole { Key, Value }

    val String.isStyled: Boolean get() = ansi.isTextStyled(this)
    val CharSequence.lengthNoAnsi: Int get() = toString().stripAnsi().length

    fun String.style(styleCode: StyleCode): String = ansi.overwriteStyle(this, styleCode)

    fun String.style(style: TextStyle, color: Colour): String =
        ansi.overwriteStyle(this, StyleTheme(style, color, BGColour.Default))

    fun String.style(style: TextStyle, color: Colour, background: BGColour): String =
        ansi.overwriteStyle(this, StyleTheme(style, color, background))

    fun String.applyStyle(style: TextStyle, color: Colour, background: BGColour): String =
        ansi.applyStyleCode(this, StyleTheme(style, color, background))

    fun String.applyStyle(styleCode: StyleCode, ): String = ansi.applyStyleCode(this, styleCode)

    fun String.stripAnsi(): String = ansi.stripAnsi(this)

    fun String.extractStyleSegments(): List<AnsiColorSegment> = ansi.extractColorSegments(this)
    fun String.applyStyleSegment(colourSegment: AnsiColorSegment): String =
        ansi.overwriteStyle(this, colourSegment)

    fun String.toPair(role: SpanRole? = null): StyledPair {
        if (!this.isStyled) {
            return StyledPair(this, role = role)
        }
        return StyledPair(this.stripAnsi(), this, role)
    }

    fun String.toMutablePair(role: SpanRole? = null): MutablePair {
        if (!this.isStyled) {
            return MutablePair(this, role = role)
        }
        return MutablePair(this.stripAnsi(), this, role)
    }

    fun mergeSpans(wrapper: TextWrapper, vararg spans: TextSpan): TextSpan {
        val builder = SpanBuilder()
        val spanList = spans.toList()
        spanList.forEachButLast(skipLastSize = 1) {
            builder.append(wrapper.wrap(it))
        }
        spanList.lastOrNull()?.let {
            builder.append(it)
        }
        return builder.toSpan()
    }

    fun Collection<String>.toMutablePairs(): List<MutablePair> = map { it.toMutablePair() }
    fun Array<out String>.toMutablePairs(): List<MutablePair> = map { it.toMutablePair() }
    fun String.stripLineBreaks(): String {
        return this.replace(lineBreakRegex, "")
    }

    fun buildSpan(builderAction: SpanBuilder.() -> Unit): StyledPair = buildTextSpan(builderAction)

    fun String.style(tag: ValueTag): TextSpan = valueStyler.styleAsPair(tag, this)
    fun TextSpan.style(tag: ValueTag): TextSpan = valueStyler.styleAsPair(tag, this.styled)

    fun String.style(tag: ActionTag): TextSpan = actionStyler.styleAsPair(tag, this)
    fun TextSpan.style(tag: ActionTag): TextSpan = actionStyler.styleAsPair(tag, this.styled)


    companion object {

        val ansi: AnsiStyler = AnsiStyler()

        val valueStyler: StyleRegistry = StyleRegistry()
        val actionStyler: StyleRegistry = StyleRegistry()

        init {
            valueStyler.addStyler(ValueTag.ClassName) { it.colorize(Colour.Blue) }
            valueStyler.addStyler(ValueTag.PropertyName) { it.colorize(Colour.Magenta) }
            valueStyler.addStyler(ValueTag.EnumClass) { it.colorize(Colour.Magenta) }
            valueStyler.addStyler(ValueTag.EnumName) { it.colorize(Colour.Cyan) }
            valueStyler.addStyler(ValueTag.Time) { it.colorize(Colour.Gray) }

            actionStyler.addStyler(ActionTag.Speech) { Emoji.Speech.append(it) }
            actionStyler.addStyler(ActionTag.Info) { Emoji.Info.prepend(it) }

        }

        private fun Any?.textParts(): List<TextPart> {
            return when (this) {
                is KProperty<*> -> listOf(TextPart.Key, TextPart.Value,)
                is Enum<*> -> listOf(TextPart.Key, TextPart.Value,)
                else -> listOf(TextPart.Value)
            }
        }

        private fun Any?.isKeyed(): Boolean {
            if(this == null){
                return false
            }
            return when (this) {
                is KProperty<*> -> true
                is Enum<*> -> true
                else -> false
            }
        }

        private fun applyToBuilder(builder:SpanBuilder, wrapper: TextWrapper, pair: MutablePair, part: TextPart){
            if(part == TextPart.Key){
                builder.append(wrapper.wrap(pair))
            }else{
                builder.append(pair)
            }
        }

        private fun formatEnumPart(enum: Enum<*>, textPart: TextPart): String {
            return when (textPart) {
                TextPart.Key -> {
                    valueStyler.styleAsString(ValueTag.EnumClass, enum::class.simpleOrAnon)
                }
                TextPart.Value -> {
                    when (enum) {
                        is NameValue -> enum.value.toString()
                        is TextContaining ->enum.asText()
                        else -> valueStyler.styleAsString(ValueTag.EnumName, enum.name)
                    }
                }
            }
        }

        private fun makeFormating(
            receiver: Any?,
            postfix: Postfix,
            textPart: TextPart? = null
        ): MutablePair {
            if (receiver == null) {
                return MutablePair(receiver.toString())
            }
            return when (receiver) {
                is KProperty0<*> -> {
                    val displayName =  receiver.displayName
                    val styledName = valueStyler.styleAsPair(ValueTag.PropertyName, displayName)
                    val result = receiver.get()
                    result?.let {
                        val styledValue = formatKnown(it, TextPart.Value)
                        val wrappedName = postfix.wrap(styledName)
                        MutablePair(wrappedName.plain + styledValue.plain, wrappedName.styled + styledValue.styled)
                    } ?: MutablePair(styledName.plain + ": N/A", styledName.styled + ": N/A")
                }
                is Enum<*> -> {
                    textPart?.let {part->
                        val wrapped = if(part == TextPart.Value){
                            formatEnumPart(receiver,  part)
                        }else{
                            postfix.wrap(formatEnumPart(receiver,  part))
                        }
                        MutablePair(ansi.stripAnsi(wrapped), wrapped)
                    }?:run {
                        val key = formatEnumPart(receiver,  TextPart.Key)
                        val value =  formatEnumPart(receiver,  TextPart.Value)
                        val wrapped = "$key: $value"
                        MutablePair(wrapped, ansi.stripAnsi(wrapped))
                    }
                }
                is Styled -> receiver.textSpan.asMutable()
                is TextSpan -> receiver.asMutable()
                else -> MutablePair(receiver.toString())
            }
        }

        fun formatKnown(receiver: Any?, textPart: TextPart? = null): TextSpan {
           val separator = if (receiver.isKeyed()){
               Postfix(": ")
           }else{
               Postfix()
           }
           return makeFormating(receiver, separator, textPart)
        }


        fun formatModifying(
            receiver: Any?,
            modification: (MutablePair) -> Unit
        ): TextSpan {
            val parts = receiver.textParts()
            val emptyPostfix = Postfix(SpecialChars.EMPTY)
            return if (parts.size > 1) {
                val builder = SpanBuilder()
                parts.forEach{ part ->
                    val span = makeFormating(receiver, emptyPostfix, part)
                    span.changeRole(part)
                    val modified = MutablePair(span.plain, role = part)
                    modification.invoke(modified)
                    if(ansi.isTextStyled(modified.styled)){
                        applyToBuilder(builder, Postfix(": "), modified, part)
                    }else{
                        applyToBuilder(builder, Postfix(": "), span, part)
                    }
                }
                builder.toSpan()
            } else {
                val part = parts.first()
                val span = makeFormating(receiver, emptyPostfix, part)
                span.changeRole(part)
                val modified = MutablePair(span.plain, role = part)
                modification.invoke(modified)
                if(ansi.isTextStyled(modified.styled)){
                    modified
                }else {
                    span
                }
            }
        }
    }
}

fun String.colorize(color: Color): String {
    val colour = TextStyler.ansi.tryTranslateJavaColor(color)
   return if(colour == Colour.Default){
        this
    }else{
        TextStyler.ansi.style(this, colour)
    }
}
fun String.colorize(colour: Colour): String = TextStyler.ansi.style(this, colour)
fun String.colorize(bgColour: BGColour): String = TextStyler.ansi.style(this, bgColour)