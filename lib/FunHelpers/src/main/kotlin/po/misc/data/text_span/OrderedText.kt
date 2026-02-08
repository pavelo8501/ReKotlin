package po.misc.data.text_span

import po.misc.collections.toArray
import po.misc.data.Postfix
import po.misc.data.Prefix
import po.misc.data.Separator
import po.misc.data.StringModifyParams
import po.misc.data.TextWrapper
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler

open class OrderedText():MutableSpan{

    constructor(vararg spans: TextSpan):this(){
        val lines =  spans.toList().map { it.asMutable() }
        linesBacking.addAll(lines)
    }
    constructor(vararg text: String):this(){
       val mutablePairs = text.map {str-> MutablePair(str.stripAnsi(), str)}
       linesBacking.addAll(mutablePairs)
    }

    internal val linesBacking: MutableList<MutableSpan> = mutableListOf()
    val lines: List<TextSpan> get() = linesBacking

    open val text: TextSpan get() = text()

    override val plain: String get() = text.plain
    override val styled: String get() = text.styled

    val lineMaxLen : Int get() =  lines.maxOf {line->  line.plainLength }
    val linesCount : Int get() = lines.size
    override val plainLength: Int get() = lines.sumOf { it.plainLength }

    override fun copy(newRole: SpanRole?): OrderedText {
       val lineCopies = lines.map { it.copy(newRole?:it.role) }
       return OrderedText(*lineCopies.toArray())
    }

    fun text(orientation: Orientation = Orientation.Vertical): TextSpan{
        return if(orientation == Orientation.Vertical){
            lines.joinSpans(Postfix(SpecialChars.NEW_LINE))
        }else{
            lines.joinSpans(Postfix(SpecialChars.WHITESPACE))
        }
    }

    fun plain(orientation: Orientation = Orientation.Vertical): String{
        if(orientation == Orientation.Vertical){
            return lines.joinToString(SpecialChars.NEW_LINE) {span-> span.plain }
        }
        return lines.joinToString(SpecialChars.WHITESPACE) {span-> span.plain }
    }
    fun styled(orientation: Orientation = Orientation.Vertical): String{
        if(orientation == Orientation.Vertical){
            return lines.joinToString(SpecialChars.NEW_LINE) { span-> span.styled }
        }
        return lines.joinToString(SpecialChars.WHITESPACE) { span-> span.styled }
    }
    fun prepend(plainText: String, styledText: String = plainText){
        linesBacking.add(0,MutablePair(plainText.stripAnsi(), styledText))
    }
    override fun prepend(other: TextSpan) {
        linesBacking.add(0, MutablePair(other))
    }
    fun append(plainText: String, styledText:String){
        linesBacking.add(MutablePair(plainText.stripAnsi(), styledText))
    }
    override fun append(other: TextSpan) {
        linesBacking.add(other.copyMutable())
    }
    fun appendAll(spans: List<TextSpan>){
        spans.forEach {
            append(it)
        }
    }
    open fun merge(other: OrderedText, separator: Separator = Separator()) {
        for(i in 0..<other.linesBacking.size){
            val thisLine = linesBacking.getOrNull(i)
            if(thisLine != null){
                thisLine.append(other.lines[i])
            }else{
                append(other.lines[i])
            }
        }
    }
    open fun append(orderedText: OrderedText){
        orderedText.lines.forEach {
            linesBacking.add( MutablePair(it))
        }
    }

    override fun change(plainText: String, styledText: String): OrderedText{
        linesBacking.clear()
        linesBacking.add(MutablePair(plainText.stripAnsi(), styledText))
        return this
    }
    override fun change(other: TextSpan):OrderedText{
        linesBacking.clear()
        when(other) {
            is OrderedText -> linesBacking.addAll(other.linesBacking)
            is TextSpan ->  linesBacking.add(other.copyMutable())
        }
        return this
    }
    override fun changeRole(newRole: SpanRole?):OrderedText{
        if(newRole != null){
            linesBacking.forEach { it.changeRole(newRole) }
        }
        return this
    }
    fun toMutable(orientation: Orientation, role: SpanRole? = null): MutableSpan{
        if(orientation == Orientation.Vertical) {
            return  lines.joinSpansAs<MutableSpan>(Postfix(SpecialChars.NEW_LINE), role)
        }
        return lines.joinSpansAs<MutableSpan>(Postfix(), role)
    }

    override fun toString(): String = plain

    operator fun get(index: Int): TextSpan {
       return lines[index]
    }

    companion object: TextStyler

}