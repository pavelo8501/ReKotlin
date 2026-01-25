package po.misc.data.text_span

import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.strings.ElementOptions
import po.misc.data.strings.ListOptions
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.interfaces.named.HasText
import po.misc.interfaces.named.Named


abstract class FormattedTextBase(
    override var plain: String = "",
    override var styled: String = plain,
):EditablePair, TextStyler{

    var overflowPrevention: Boolean = false
    val hasLineBreaks:Boolean get() = plain.contains(SpecialChars.NEW_LINE)

    internal var subEntries = mutableListOf<TextSpan>()
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

    private fun joinRecursively(rootEntry:FormattedText, parameters: ListOptions, level: Int){
        for(entry in subEntries){
            val indentation = parameters.prefix.repeat(level)
            rootEntry.append("${indentation}${entry.plain}", "${indentation}${entry.styled}", parameters.separator)
            if(entry is FormattedTextBase){
                entry.joinRecursively(rootEntry, parameters, level + 1)
            }
        }
    }
    private fun joinRecursively(rootEntry:FormattedTextBase, separator: String){
        for(entry in subEntries){
            rootEntry.append(entry.plain, entry.styled, separator)
            if(entry is FormattedTextBase){
                entry.joinRecursively(rootEntry, separator)
            }
        }
    }

    override fun append(plainText: String, styledText: String) {
        appendPlain(plainText)
        appendFormatted(styledText)
    }

    override fun prependPlain(prefix: String, separator:String): FormattedTextBase{
        if(prefix.isBlank()){ return this }
        plain = "${prefix}${separator}${plain}"
        return this
    }
    override fun prependFormatted(prefix: String, separator:String , styleCode: StyleCode? ):FormattedTextBase{
        if(prefix.isBlank()){ return this }
        styled = if(styleCode != null){
            "${prefix.style(styleCode)}${separator}${styled}"
        }else{
            "${prefix}${separator}${styled}"
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
        styled = if(styleCode != null){
            "${styled}${separator}${text.style(styleCode)}"
        }else{
            "${styled}${separator}${text}"
        }
        return this
    }

    fun appendAll(formatted : List<TextSpan>, parameters: StringifyOptions):FormattedTextBase{
        formatted.forEach {
            append(it, parameters)
        }
        return this
    }

    override fun append(other: TextSpan){
        append(other.styled)
    }
    override fun prepend(other: TextSpan){
        prepend(other.styled)
    }

    override fun prepend(plainText: String, styledText: String) {
        prependPlain(plainText)
        prependFormatted(styledText)
    }

    fun styleFormatted(styleCode: StyleCode? = null): FormattedTextBase{
        if(styleCode != null){
            styled = styled.style(styleCode)
        }
        return this
    }

    fun add(plain:String, formatted: String):FormattedTextBase{
     //   subEntries.add(FormattedText(plain, formatted))
        return this
    }
    fun add(namedFormatted: NamedFormattedText):FormattedTextBase{
        namedFormattedBacking.add(namedFormatted)
        return this
    }

    override fun write(plainString: String, formattedString: String):FormattedTextBase{
        plain = plainString
        styled = formattedString
        return this
    }

    override fun change(plainText: String, styledText: String){
        plain = plainText
        styled = styledText
    }

    fun write(pair: NamedFormattedText):FormattedTextBase{
        return write(pair.plain, pair.styled)
    }
    override fun writePlain(plainString: String):FormattedTextBase{
        plain = plainString
        styled = plainString
        return this
    }
    override fun writeFormatted(formattedString: String):FormattedTextBase{
        plain = formattedString.stripAnsi()
        styled = formattedString
        return this
    }

    fun addNotNull(subEntry: TextSpan?):FormattedTextBase{
        if(subEntry != null){
            subEntries.add(subEntry)
        }
        return this
    }
    fun addAll(subEntries: List<TextSpan>) : FormattedTextBase {
        //subEntries.forEach { add(it) }
        return this
    }
    fun addAll(vararg namedFormatted: NamedFormattedText):FormattedTextBase{
        namedFormattedBacking.addAll(namedFormatted)
        return this
    }
    fun joinSubEntries(parameters: ListOptions): EditablePair {
        val rootEntry = FormattedText(plain, styled)
        joinRecursively(rootEntry, parameters, parameters.indent)
        return rootEntry
    }
    fun joinSubEntries(parameters: ElementOptions):EditablePair {
        val rootEntry = FormattedText(plain, styled)
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
               // append(it, useSeparator)
            }
        }
        return this
    }
    override fun toString(): String = styled
}

class FormattedText(
    plain: String? = null,
    formatted: String? = plain,
):FormattedTextBase(plain?:"", formatted?:""){

    constructor(hasText: HasText,  formatted: String = hasText.value): this(hasText.value, formatted)
    constructor(textSpan : TextSpan): this(textSpan.plain, textSpan.styled)

    fun output(){
        print(styled)
    }
    override fun toString(): String = styled

}

class NamedFormattedText(
    val name: Named,
    plain: String = "",
    formatted: String = plain,
    val separator: ExtendedString? = null
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

fun Named.createFormatted(pair: TextSpan):NamedFormattedText{
    return NamedFormattedText(this, pair.plain, pair.styled)
}