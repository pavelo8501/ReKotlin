package po.misc.data.strings

import po.misc.data.HasText
import po.misc.data.Named
import po.misc.data.pretty_print.parts.options.ExtendedString
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler


interface FormattedPair{
    val plain: String
    val formatted: String

    val plainLength: Int get() = plain.length
    val formattedLength: Int get() = formatted.length
    val totalPlainLength: Int

}

interface EditablePair: FormattedPair{
    val namedFormatted: List<NamedFormattedText>

    val hasNamed:Boolean get() = namedFormatted.isNotEmpty()

    fun appendPlain(text: String, separator:String = SpecialChars.EMPTY):EditablePair
    fun appendFormatted(text: String,  styleCode: StyleCode? = null,  separator:String = SpecialChars.EMPTY):EditablePair

    fun append(pair :FormattedPair, separator: String):EditablePair{
        appendPlain(pair.plain, separator)
        appendFormatted(pair.formatted, null,  separator)
        return this
    }
    fun append(formatted :FormattedPair, parameters: StringifyOptions):EditablePair{
        appendPlain(formatted.plain, parameters.separator)
        appendFormatted(formatted.formatted, null,  parameters.separator)
        return this
    }
    fun append(plainText:String, formattedText: String, separator:String = SpecialChars.EMPTY):EditablePair{
        appendPlain(plainText, separator)
        appendFormatted(formattedText, styleCode = null, separator)
        return this
    }
    fun append(text: String, separator:String = SpecialChars.EMPTY,  styleCode: StyleCode? = null){
        appendPlain(text, separator)
        appendFormatted(text, styleCode, separator)
    }

    fun prependPlain(text: String, separator:String = SpecialChars.EMPTY): EditablePair
    fun prependFormatted(text: String,  separator:String = SpecialChars.EMPTY, styleCode: StyleCode? = null):EditablePair
    fun prepend(formattedPair :FormattedPair, separator:String){
        prependPlain(formattedPair.plain, separator)
        prependFormatted(formattedPair.formatted,  separator)
    }
    fun prepend(text: String, styleCode: StyleCode? = null, separator:String = SpecialChars.EMPTY){
        prependPlain(text, separator)
        prependFormatted(text, separator, styleCode)
    }


    fun joinSubEntries(separator: String? = null):FormattedTextBase

    fun joinSubEntries(separatorChar: Char):FormattedTextBase{
       return joinSubEntries(separator = separatorChar.toString())
    }
    fun joinSubEntries(separator: ExtendedString):FormattedTextBase{
        return joinSubEntries(separator = separator.text)
    }

    fun joinNamedEntries(namedList: List<Named>, separator: String? = null):EditablePair

    fun joinNamedEntries(separator: String, vararg  names : Named):EditablePair {
        return joinNamedEntries(namedList = names.toList(), separator = separator)
    }
    fun joinNamedEntries(vararg  names : Named):EditablePair {
       return  joinNamedEntries(namedList = names.toList(), separator = null)
    }


    fun write(plainString: String, formattedString: String):EditablePair
    fun writePlain(plainString: String):EditablePair
    fun writeFormatted(formattedString: String):EditablePair

    fun getNamed(named: Named):NamedFormattedText?{
       return namedFormatted.firstOrNull{ it.name == named }
    }

}

abstract class FormattedTextBase(
    override var plain: String = "",
    override var formatted: String = plain,
):EditablePair, TextStyler{

    var overflowPrevention: Boolean = false
    internal var subEntries = mutableListOf<FormattedPair>()
    internal val formattedTextSubEntries: List<FormattedText> get() = subEntries.filterIsInstance<FormattedText>()

    internal val namedFormattedBacking = mutableListOf<NamedFormattedText>()
    override val namedFormatted: List<NamedFormattedText> get() = namedFormattedBacking

    private var namedAlreadyJoined: Boolean = false


    final override val totalPlainLength: Int get() {
        val subSize = subEntries.sumOf {subEnty-> subEnty.plainLength }
        return if(!namedAlreadyJoined){
            val namedSize =  namedFormatted.sumOf {subNamed-> subNamed.plainLength }
            namedSize + subSize + plainLength
        }else{
            subSize + plainLength
        }
    }

    private fun joinRecursively(rootEntry:FormattedText, parameters: StringifyOptions.ListOptions, level: Int){
        for(entry in subEntries){
            val indentation = parameters.indentWith.repeat(level)
            rootEntry.append("${indentation}${entry.plain}", "${indentation}${entry.formatted}", parameters.separator)
            if(entry is FormattedTextBase){
                entry.joinRecursively(rootEntry, parameters, level + 1)
            }
        }
    }
    private fun joinRecursively(rootEntry:FormattedTextBase, separator: String){
        for(entry in subEntries){
            rootEntry.append(entry.plain, entry.formatted, separator)
            if(entry is FormattedTextBase){
                entry.joinRecursively(rootEntry, separator)
            }
        }
    }

    override fun prependPlain(text: String, separator:String): FormattedTextBase{
        plain = "${text}${separator}${plain}"
        return this
    }
    override fun prependFormatted(text: String, separator:String , styleCode: StyleCode? ):FormattedTextBase{
        formatted = if(styleCode != null){
            "${text.style(styleCode)}${separator}${formatted}"
        }else{
            "${text}${separator}${formatted}"
        }
        return this
    }

    override fun appendPlain(text: String, separator:String): EditablePair{
        plain = "${plain}${separator}${text}"
        return this
    }
    override fun appendFormatted(
        text: String,
        styleCode: StyleCode?,
        separator:String
    ):EditablePair{
        formatted = if(styleCode != null){
            "${formatted}${separator}${text.style(styleCode)}"
        }else{
            "${formatted}${separator}${text}"
        }
        return this
    }

    fun appendAll(formatted : List<FormattedPair>, parameters: StringifyOptions):FormattedTextBase{
        formatted.forEach {
            append(it, parameters)
        }
        return this
    }

    fun styleFormatted(styleCode: StyleCode? = null): FormattedTextBase{
        if(styleCode != null){
            formatted = formatted.style(styleCode)
        }
        return this
    }

    fun add(subEntry: FormattedPair):FormattedTextBase{
        subEntries.add(subEntry)
        return this
    }
    fun add(plain:String, formatted: String):FormattedTextBase{
        subEntries.add(FormattedText(plain, formatted))
        return this
    }
    fun add(namedFormatted: NamedFormattedText):FormattedTextBase{
        namedFormattedBacking.add(namedFormatted)
        return this
    }

    override fun write(plainString: String, formattedString: String):FormattedTextBase{
        plain = plainString
        formatted = formattedString
        return this
    }
    fun write(pair: FormattedPair):FormattedTextBase{
        return write(pair.plain, pair.formatted)
    }
    override fun writePlain(plainString: String):FormattedTextBase{
        plain = plainString
        formatted = plainString
        return this
    }
    override fun writeFormatted(formattedString: String):FormattedTextBase{
        plain = formattedString.stripAnsi()
        formatted = formattedString
        return this
    }

    fun addNotNull(subEntry: FormattedPair?):FormattedTextBase{
        if(subEntry != null){
            subEntries.add(subEntry)
        }
        return this
    }
    fun addAll(subEntries: List<FormattedPair>) : FormattedTextBase {
        subEntries.forEach { add(it) }
        return this
    }
    fun addAll(vararg namedFormatted: NamedFormattedText):FormattedTextBase{
        namedFormattedBacking.addAll(namedFormatted)
        return this
    }
    fun joinSubEntries(parameters: StringifyOptions.ListOptions): EditablePair {
        val rootEntry = FormattedText(plain, formatted)
        joinRecursively(rootEntry, parameters, parameters.indent)
        return rootEntry
    }
    fun joinSubEntries(parameters: StringifyOptions.ElementOptions):EditablePair {
        val rootEntry = FormattedText(plain, formatted)
        joinRecursively(rootEntry, parameters.separator)
        return rootEntry
    }
    override fun joinSubEntries(separator: String?):FormattedTextBase{
        if(namedFormatted.isNotEmpty()){
            val list = namedFormatted.map { it.name }
            joinNamedEntries(list, separator)
        }
        if(subEntries.isNotEmpty()){
            joinRecursively(this, separator?: SpecialChars.EMPTY)
        }
        return this
    }
    fun joinSubEntries(separatorAny: Any):FormattedTextBase = joinSubEntries(separator = separatorAny.toString())

    override fun joinNamedEntries(namedList: List<Named>, separator: String? ):FormattedTextBase {
        val filtered = namedFormatted.filter { it.name in  namedList }
        if(filtered.isEmpty()){
            return this
        }
        namedAlreadyJoined = true
        val first = filtered.first()
        val useSeparator = separator?:first.separator?.text?:SpecialChars.EMPTY
        write(first)
        if(filtered.size > 1){
            filtered.drop(1).forEach {
                append(it, useSeparator)
            }
        }
        return this
    }
    override fun toString(): String = formatted
}

class FormattedText(
   plain: String? = null,
   formatted: String? = plain,
):FormattedTextBase(plain?:"", formatted?:""){
    constructor(hasText: HasText,  formatted: String = hasText.value): this(hasText.value, formatted)
    constructor(formattedText : FormattedText): this(formattedText.plain, formattedText.formatted)
    override fun toString(): String = formatted
}

class NamedFormattedText(
    val name: Named,
    plain: String = "",
    formatted: String = plain,
    val separator:ExtendedString? = null
): FormattedTextBase(plain, formatted){

    override val plainLength: Int get() {
       return plain.length + (separator?.displaySize?:0)
    }
}

fun Named.createFormatted(plainText:String, formattedText: String = plainText):NamedFormattedText{
    return NamedFormattedText(this, plainText, formattedText)
}

fun Named.createFormatted(separator: ExtendedString, plainText:String, formattedText: String = plainText):NamedFormattedText{
    return NamedFormattedText(this, plainText, formattedText, separator)
}

fun Named.createFormatted(pair: FormattedPair):NamedFormattedText{
    return NamedFormattedText(this, pair.plain, pair.formatted)
}