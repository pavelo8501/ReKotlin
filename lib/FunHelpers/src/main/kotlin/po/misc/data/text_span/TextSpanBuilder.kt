package po.misc.data.text_span

import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.styles.TransformParameters
import po.misc.reflection.displayName
import kotlin.reflect.KProperty0

class SpanBuilder(
    internal var mutableSpan : MutableSpan = MutablePair()
):TextStyler {

    internal fun setNew(mutable: MutableSpan){
        mutableSpan = mutable
    }
    fun append(value: Any):SpanBuilder{
        val span =  TextStyler.knownClassFormatting(value)
        mutableSpan.append(span)
        return this
    }

    fun append(span: TextSpan){
        mutableSpan.append(span)
    }
    fun prepend(span: TextSpan){
        mutableSpan.prepend(span)
    }
    fun appendLine(value: TextSpan){
        mutableSpan.append((value.plain + SpecialChars.NEW_LINE), (value.styled + SpecialChars.NEW_LINE))
    }
    fun prependLine(value: TextSpan){
        mutableSpan.prepend((value.plain + SpecialChars.NEW_LINE), (value.styled + SpecialChars.NEW_LINE))
    }

    fun append(list: List<*>, separator: String = ", "):SpanBuilder{
        val span = TextStyler.knownClassFormatting(list, separator = separator)
        mutableSpan.append(span)
        return this
    }
    fun append(prop: KProperty0<*>):SpanBuilder{
        val key = prop.displayName
        val value = prop.get()
        val span =  TextStyler.knownClassFormatting(value){
            "$key: $it"
        }
        mutableSpan.append(span)
        return this
    }

    fun completeAndReturn():MutableSpan{
        return mutableSpan
    }
    fun toPair():StyledPair = mutableSpan.copy()
}


fun  List<MutableSpan>.withSpansBuilding(block: SpanBuilder.(MutableSpan)->Unit):List<MutableSpan>{
    val result = mutableListOf<MutableSpan>()
    val builder = SpanBuilder()
    forEach {element->
        builder.setNew(element)
        block.invoke(builder, element)
        result.add(element)
    }
    return result
}

fun buildTextSpan(builderAction: SpanBuilder.()-> Unit): StyledPair{
    val builder = SpanBuilder()
    builderAction.invoke(builder)
    return builder.toPair()
}
