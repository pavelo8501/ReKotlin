package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.Styled
import po.misc.data.TextBuilder
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.strings.ElementOptions
import po.misc.data.strings.ListOptions
import po.misc.data.strings.StringifyOptions
import po.misc.data.strings.stringification
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.TextSpan
import po.misc.debugging.ClassResolver


sealed interface OutputOptions{
    val name : TextSpan?
}

    
internal class OutputParameters(
    override val name:TextSpan,
    val content: Any?,
    val styleCode: StyleCode? = null
):OutputOptions{

    constructor(
        name:TextSpan,
        styleCode: StyleCode? = null,
        preserveAnsi: Boolean = true,
        builderAction: StringBuilder.() -> Unit
    ):this(name, null, styleCode) {
        stringBuilderAction = builderAction
        this.preserveAnsi = preserveAnsi
    }

    val size:Int = name.plainLength
    var preserveAnsi:Boolean = false
    var stringBuilderAction: (StringBuilder.() -> Unit)? = null
    fun preserveAnsi():OutputParameters{
        preserveAnsi = true
        return this
    }
}

class OutputCompare<out T>(
    val object1:T,
    val object2:T,
    override var name:TextSpan?
): OutputOptions{

    var prefix:String? = null

    fun withPrefix(prefixCompare : String?):OutputCompare<T>{
        prefix = prefixCompare
        return this
    }
}


internal fun Any.isTextSpan():TextSpan? =
    when(this) {
        is TextSpan -> this
        is Styled -> this.textSpan
        else -> null
    }


@PublishedApi
internal fun outputInternal(
    receiver: Any?,
    prefix: String? = null,
    colour: Colour? = null
){
    checkDispatcher()

    receiver?.isTextSpan()?.let {span->
        prefix?.let {
            println("$it ->")
            println(span.styled)
        }
    }?:run {
        val result = receiver.stringify(colour)
        prefix?.let {
            if(result.hasLineBreak){
                println("$it ->")
            }else{
                print("$it -> ")
            }
        }
        println(result.styled)
    }
}

@PublishedApi
internal fun outputInternal(
    options: OutputOptions
){
    checkDispatcher()
    when(options){
        is OutputParameters -> {

            val strBuildAction = options.stringBuilderAction
            val contentByBuilder = if (strBuildAction != null){
                val builder = StringBuilder()
                strBuildAction.invoke(builder)
                builder.toString()
            }else{
                null
            }
            val content = options.content?:contentByBuilder?:"null"
            content.isTextSpan()?.let {
                println("${options.name.styled} ${Emoji.Speech} ")
                println(it.styled)
            }?:run {

                val opts = ElementOptions("", SpecialChars.EMPTY, options.styleCode)
                opts.preserveAnsi = options.preserveAnsi
                val styled =  stringification(content,  opts)
                if(styled.styled.contains(SpecialChars.NEW_LINE)){
                    println("${options.name.styled} ${Emoji.Speech} ")
                    println(styled.styled)
                }else{
                    print("${options.name.styled} ${Emoji.Speech} ")
                    println(styled.styled)
                }
            }
        }
        is OutputCompare<*> -> {
            val firsObj = options.object1?.let {
                ClassResolver.instanceInfo(it).formattedString
            }?:"null"

            val secondObj = options.object2?.let {
                ClassResolver.instanceInfo(it).formattedString
            }?:"null"
            options.prefix?.let {
                println(it)
            }
            println(firsObj)
            println(secondObj)
        }
    }
}

@PublishedApi
internal fun outputInternal(
    context: TraceableContext,
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null
) {
    checkDispatcher()
    val info = ClassResolver.instanceInfo(context)

    if (receiver != null) {
        when (receiver) {
            is List<*> -> {
                println(info.formattedString)
                receiver.output(prefix = prefix, colour = colour)
            }
            is TextSpan -> {
                println(receiver.styled)
            }
            else -> {
                println(info.formattedString)
                val formattedEntry = receiver.stringify(prefix)
                println(formattedEntry.styled)
            }
        }
    } else {
        println("${info.formattedString} output null")
    }
}

fun Any?.output(colour: Colour? = null): Unit = outputInternal(this, prefix = null, colour = colour)
fun Any?.output(prefix: String, colour: Colour? = null): Unit = outputInternal(this, prefix = prefix,  colour)

fun <T> T.outputCompare(other:T, prefix:String? = null){
    outputInternal(OutputCompare(this, other, null).withPrefix(prefix))
}

internal fun Any?.output(enabled: Boolean, colour: Colour? = null){
    if(enabled){
        outputInternal(this, prefix = null, colour = colour)
    }
    return
}

inline fun <reified T: Any> T.output(prefix: String,  noinline configAction: ListOptions.(T) -> Unit){
     val options = ListOptions()
     options.header = prefix
     configAction.invoke(options, this)
     val styled = stringification(this, options)
     println(styled.toString())
}
