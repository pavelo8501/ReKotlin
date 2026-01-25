package po.misc.data

import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.styles.StringFormatter
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.types.k_class.readAllProperties


interface PrettyPrint: TextBuilder {
    val formattedString: String
    companion object:  StringFormatter()
}

interface Styled : TextSpan {

    val textSpan: TextSpan

    override val plain: String get() = textSpan.plain
    override val styled: String get() = textSpan.styled

//    fun styledPair(plainText: String, styledText: String = plainText):StyledPair{
//      return  StyledPair(TextStyler.stripAnsi(plainText), styledText)
//    }

}


interface PrettyFormatted {
    fun formatted(renderOnly: List<RowID>? = null): String
    fun formatted(vararg renderOnly: RowID): String = formatted(renderOnly.toList())
}

inline  fun <reified T:PrettyPrint> T.snapshot():List<String>{
    val snap =  this@snapshot as Any
    val res =  snap::class.readAllProperties(snap)
    return res
}



