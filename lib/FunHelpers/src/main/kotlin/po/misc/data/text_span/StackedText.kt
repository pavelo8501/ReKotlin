package po.misc.data.text_span

import po.misc.collections.toList
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler

/**
 * Mutable text container used during assembly phases.
 *
 * Unlike [TextSpan], operations mutate internal state and do not
 * produce new instances. Intended for builders and assemblers only.
 */
interface MutableSpan: TextSpan{
    fun prepend(other: TextSpan)
    fun prepend(plainText: String, styledText:String = plainText)
    fun append(other: TextSpan)
    fun append(plainText: String, styledText:String = plainText)
    fun change(plainText: String, styledText:String = plainText)
}


sealed class MutablePairBase(
//    initialPlain: String = "",
//    initialStyled: String = initialPlain
):MutableSpan, TextStyler{

    //override var plain: String = initialPlain

    abstract override var plain: String

    abstract override var styled: String



    override fun prepend(other: TextSpan){
        plain = "${other.plain}${plain}"
        styled = "${other.styled}${styled}"
    }
    override fun append(other: TextSpan){
        plain= "${plain}${other.plain}"
        styled = "${styled}${other.styled}"
    }
    override fun append(plainText: String, styledText:String){
        plain += plainText.stripAnsi()
        styled += styledText
    }
    override fun prepend(plainText: String, styledText:String){
        plain = "${plainText.stripAnsi()}${plain}"
        styled = "${styledText}${styled}"
    }
    override fun change(plainText: String, styledText:String){
        plain = plainText.stripAnsi()
        styled = styledText
    }
}

class MutablePair(
    initialPlain: String = "",
    initialStyled: String = initialPlain
):MutablePairBase(){

    constructor(textSpan: TextSpan):this(textSpan.plain, textSpan.styled)

    override var plain: String = initialPlain
    override var styled: String = initialStyled
}

interface OrderedText : TextSpan{
    val text: List<TextSpan>

    val plainMaxLength: Int get() = text.maxOfOrNull { it.plainLength }?:0
    val styledMaxLength: Int get() = text.maxOfOrNull { it.styledLength }?:0

    fun append(other: TextSpan)
    fun appendAll(spans: List<TextSpan>){
        spans.forEach {
            append(it)
        }
    }

    fun toLinesPlain(): String
    fun toLinesStyled(): String

}

class OrderedPairs(
    textSpan: TextSpan
):MutablePairBase(), OrderedText{

    private val textBacking: MutableList<TextSpan> = mutableListOf()
    override val text: List<TextSpan> get() =  textBacking

    override var plain: String get() = readPlain()
        set(value) {}
    override var styled: String get() = readStyled()
        set(value) {}

    init {
        textBacking.add(textSpan)
    }

    private fun readPlain(): String{
       return text.joinToString(SpecialChars.EMPTY) {
            it.plain
        }
    }
    private fun readStyled(): String{
        return text.joinToString(SpecialChars.EMPTY) {
            it.styled
        }
    }
    override fun append(other: TextSpan) {
        textBacking.add(other)
    }
    override fun prepend(other: TextSpan) {
        textBacking.add(0,  other)
    }

    override fun toLinesPlain(): String{
       return text.joinToStringIndexed(separator = SpecialChars.NEW_LINE) { index, value->
           "$index )  ${value.plain}"
        }
    }
    override fun toLinesStyled(): String{
        return text.joinToStringIndexed(separator = SpecialChars.NEW_LINE) { index, value->
            "$index )  ${value.styled}"
        }
    }
}


fun <T> List<T>.joinToStringIndexed(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    transform: (Int, T) -> CharSequence
): String {
    val builder = StringBuilder()
    builder.append(prefix)
    forEachIndexed { index, element ->
        val isLast = index == lastIndex
        val result =  transform.invoke(index, element)
        if(!isLast){
            builder.append(result)
            builder.append(separator)
        }else{
            builder.append(result)
        }
    }
    builder.append(postfix)
    return builder.toString()
}

fun TextSpan.toMutablePair():MutablePair =
    when(val span =  this){
        is MutablePair -> span
        is OrderedText -> span.toMutablePair()
        is TextSpan -> MutablePair(span.plain, span.styled)
    }

fun List<TextSpan>.toMutablePairs(): List<MutablePair> = this.map { it.toMutablePair() }

infix fun MutableSpan.append(span: TextSpan): Unit = this.append(other =  span)
infix fun MutableSpan.prepend(span: TextSpan): Unit = this.prepend(other =  span)

fun MutableSpan.joinAndAppend(vararg spans: TextSpan): Unit{
    val joined = spans.toList().joinSpans(Orientation.Horizontal)
    append(joined)
}








