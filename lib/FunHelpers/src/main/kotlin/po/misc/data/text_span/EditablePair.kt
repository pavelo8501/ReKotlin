package po.misc.data.text_span

import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.interfaces.named.Named


interface EditablePair: MutableSpan {

    val namedFormatted: List<NamedFormattedText>
    val hasNamed:Boolean get() = namedFormatted.isNotEmpty()

    val totalPlainLength: Int

    fun appendPlain(text: String, separator:String = SpecialChars.EMPTY):EditablePair
    fun appendFormatted(text: String,  styleCode: StyleCode? = null,  separator:String = SpecialChars.EMPTY):EditablePair

    fun append(pair :TextSpan, separator: String):EditablePair{
        appendPlain(pair.plain, separator)
        appendFormatted(pair.styled, null,  separator)
        return this
    }
    fun append(formatted :TextSpan, parameters: StringifyOptions):EditablePair{
        appendPlain(formatted.plain, parameters.separator)
        appendFormatted(formatted.styled, null,  parameters.separator)
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

    fun prependPlain(prefix: String, separator:String = SpecialChars.EMPTY): EditablePair
    fun prependFormatted(prefix: String,  separator:String = SpecialChars.EMPTY, styleCode: StyleCode? = null):EditablePair
    fun prepend(TextSpan :TextSpan, separator:String){
        prependPlain(TextSpan.plain, separator)
        prependFormatted(TextSpan.styled,  separator)
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
