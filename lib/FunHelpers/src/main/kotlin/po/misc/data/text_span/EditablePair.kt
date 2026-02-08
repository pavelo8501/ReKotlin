package po.misc.data.text_span

import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.interfaces.named.Named


interface EditablePair{

//    val namedFormatted: List<NamedFormattedText>
//    val hasNamed:Boolean get() = namedFormatted.isNotEmpty()
    
    fun appendPlain(text: String, separator:String = SpecialChars.EMPTY):EditablePair
    fun appendFormatted(text: String,  styleCode: StyleCode? = null,  separator:String = SpecialChars.EMPTY):EditablePair

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
//    fun joinSubEntries(separator: String? = null):FormattedTextBase

    fun write(plainString: String, formattedString: String):EditablePair
    fun writePlain(plainString: String):EditablePair
    fun writeFormatted(formattedString: String):EditablePair

}
