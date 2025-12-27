package po.misc.data.strings

import po.misc.context.CTX
import po.misc.data.HasText
import po.misc.data.HasValue
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.TextContaining
import po.misc.data.containsAnyOf
import po.misc.data.isUnset
import po.misc.data.output.output
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.Colour.RESET
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.reflect.KClass

interface FormattedPair{
    val plain: String
    val formatted: String
}

class FormattedText(
    override var plain: String = "",
    override var formatted: String = plain,
):FormattedPair, TextStyler{

    constructor(hasText: HasText,  formatted: String = hasText.value): this(hasText.value, formatted)

    var overflowPrevention: Boolean = false
    private var subEntries = mutableListOf<FormattedPair>()

    @PublishedApi
    internal fun applyText(plainText:String, formattedText: String, separator:String):FormattedText{
        plain = "$plain${separator}$plainText"
        formatted = "$formatted${separator}$formattedText"
        return this
    }

    fun applyFormatted(formatted :FormattedPair, parameters: StringifyOptions):FormattedText{
        applyText(formatted.plain, formatted.formatted, parameters.separator)
        return this
    }
    fun applyFormatted(formatted : List<FormattedPair>, parameters: StringifyOptions):FormattedText{
        formatted.forEach {
            applyFormatted(it, parameters)
        }
        return this
    }

    fun style(styleCode: StyleCode? = null):FormattedText{
        formatted = style(formatted,  styleCode)
        return this
    }
    fun add(subEntry: FormattedPair):FormattedText{
        subEntries.add(subEntry)
        return this
    }
    fun addAll(subEntries: List<FormattedPair>) : FormattedText {
        subEntries.forEach { add(it) }
        return this
    }

    private fun joinRecursively(rootEntry:FormattedText, parameters: StringifyOptions.ListOptions, level: Int){
        for(entry in subEntries){
            val indentation = parameters.indentWith.repeat(level)
            rootEntry.applyText("${indentation}${entry.plain}", "${indentation}${entry.formatted}", parameters.separator)
            if(entry is FormattedText){
                entry.joinRecursively(rootEntry, parameters, level + 1)
            }
        }
    }
    fun joinSubEntries(parameters: StringifyOptions.ListOptions):FormattedText {
        val rootEntry = FormattedText(plain, formatted)
        joinRecursively(rootEntry, parameters, parameters.indent)
        return rootEntry
    }
    fun applyPrefix(prefix:String?, style: StyleCode? = null):FormattedText{
        if(prefix != null){

            plain = "${style(prefix, style)}$plain"
            formatted =   if(style != null){
                "${style(prefix, style)}$formatted"
            }else{
                "${prefix}$formatted"
            }
        }
        return this
    }
    override fun toString(): String = formatted
}


interface StringFormatter{
    fun formatKnownTypes(receiver: Any?): FormattedText = Companion.formatKnownTypes(receiver)
    companion object{
        private val ANSI_REGEX = Regex("\\u001B\\[[;\\d]*m")
        fun isStyled(text:String):Boolean {
            return text.contains(RESET.code)
        }
        fun stripAnsiIfAny(text:String):String {
            if(isStyled(text)){
                stripAnsi(text)
            }
            return text
        }
        fun stripAnsi(text: String): String = text.replace(ANSI_REGEX, "")
        fun hasStyles(text: String): Boolean{
            return text.containsAnyOf(TextStyle.Reset.code)
        }
        fun style(text: String, styleCode: StyleCode?): String {
            if(styleCode != null){
                if(hasStyles(text)){
                    stripAnsi(text)
                }
                return "${styleCode.code}$text${TextStyle.Reset.code}"
            }else{
                return text
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
    }
}