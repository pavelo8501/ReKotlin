package po.misc.data.text_span

import po.misc.collections.ElementPosition
import po.misc.collections.flattenVarargs
import po.misc.collections.forEachPositioned
import po.misc.data.Postfix
import po.misc.data.TextWrapper
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler


class SpanBuilder(): TextStyler {

    constructor(mutableSpan : MutableSpan):this(){
        setNew(mutableSpan)
    }
    private var wrapper: TextWrapper? = null
    internal var mutable : MutablePair = MutablePair()

    internal fun setNew(mutableSpan: MutableSpan){
        mutable = mutableSpan as? MutablePair ?: MutablePair(mutableSpan)
    }

    fun useModifier(textWrapper: TextWrapper){
        wrapper = textWrapper
    }

    fun prepend(span: TextSpan){
        mutable.prepend(span)
    }
    fun prependLine(value: TextSpan){
        mutable.prepend((value.plain + SpecialChars.NEW_LINE), (value.styled + SpecialChars.NEW_LINE))
    }

    fun append(value: Any):SpanBuilder{
        when(value){
            is TextSpan -> append(span = value)
            else -> {
                mutable.append(value.toString())
            }
        }
        return this
    }

    fun append(text: String, styledText:String = text, textWrapper: TextWrapper? = null){
        val useWrapper = wrapper?:textWrapper
        if(useWrapper == null) {
            mutable.append(text, styledText)
        }else{
            val plainModified = useWrapper.wrap(text)
            val styledModified = useWrapper.wrap(styledText)
            mutable.append(plainModified, styledModified)
        }
    }
    fun append(span: TextSpan){
        mutable.append(span)
        if(mutable.role == null){
            mutable.changeRole(span.role)
        }else{
            mutable.append(span)
        }
    }

    fun appendLine(value: Any){
        val text = value.toString()
        mutable.append(text + SpecialChars.NEW_LINE)
    }
    fun appendLine(span: TextSpan){
        mutable.append((span.plain + SpecialChars.NEW_LINE), (span.styled + SpecialChars.NEW_LINE))
    }
    fun appendLineAll(spans: List<TextSpan>){
        spans.forEach {
            mutable.append(it)
        }
        mutable.append(SpecialChars.NEW_LINE)
    }

    fun toSpan(newRole: SpanRole? = null):StyledPair = mutable.toSpan(newRole)

    fun toMutableSpan(newRole: SpanRole? = null): MutablePair {
        val copy = mutable.copyMutable(newRole)
        return copy
    }
}

private fun inlineStyling(vararg parameters: Any): List<TextSpan>{
    val flattened = parameters.flattenVarargs()
    val postfix = if(flattened.size > 1){
        Postfix(SpecialChars.WHITESPACE)
    }else{
        Postfix(SpecialChars.EMPTY)
    }
    val spans = mutableListOf<TextSpan>()
    flattened.forEachPositioned{ position, any ->
        if(position != ElementPosition.Last){
           val styled = TextStyler.formatKnown(any)
           spans.add(postfix.wrap(styled))
        }else{
            spans.add(TextStyler.formatKnown(any))
        }
    }
    return spans
}

fun SpanBuilder.appendStyling(vararg parameters: Any){
    val spans = inlineStyling(*parameters)
    spans.forEach { append(it) }
}

fun SpanBuilder.appendLineStyling(vararg parameters: Any){
    val spans = inlineStyling(*parameters)
    appendLineAll(spans)
}

fun buildTextSpan(builderAction: SpanBuilder.()-> Unit): StyledPair {
    val builder = SpanBuilder()
    builderAction.invoke(builder)
    return builder.toSpan()
}
