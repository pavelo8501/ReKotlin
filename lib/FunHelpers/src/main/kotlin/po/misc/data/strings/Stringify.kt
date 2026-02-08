package po.misc.data.strings

import po.misc.collections.flattenVarargs
import po.misc.data.Postfix
import po.misc.data.Separator
import po.misc.data.TextWrapper
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.joinSpans
import kotlin.reflect.KProperty1




sealed class StringifyOptions(
    var prefix:String = "",
    var separator:String,

    val styleCode: StyleCode?
){
    open val indent: Int = 0
    var preserveAnsi: Boolean = false

    fun preserveAnsi(value: Boolean):StringifyOptions {
        preserveAnsi = value
        return this
    }
    fun normalizedPrefix():String = normalizedPrefix(this)

    companion object {
            val defaultIndention: ListOptions = ListOptions(indentWith = " ", indent = 1)
            val noIndention: ListOptions get() = ListOptions(indentWith = "", indent = 0)

            fun normalizedPrefix(opts:StringifyOptions): String {
                if (opts is ListOptions) {
                    if (opts.indent == 0) {
                        return "${opts.prefix.trim()} "
                    }
                    return opts.prefix
                } else {
                    if (opts.prefix.isNotBlank()) {
                        return "${opts.prefix.trim()} "
                    }
                    return ""
                }
            }

            fun elementOptions(prefix: String = "",  separator:String,  styleCode: StyleCode? = null): ElementOptions {
                return ElementOptions(prefix, separator, styleCode)
            }

            fun createForReceiver(
                receiver: Any?,
                prefix: String?,
                styleCode: StyleCode?
            ):StringifyOptions {
                return if (receiver is List<*>) {
                    if (prefix != null) {
                        ListOptions(prefix, SpecialChars.NEW_LINE, 0,  styleCode)
                    } else {
                        ListOptions("", SpecialChars.NEW_LINE, 0, styleCode)
                    }
                } else {
                    elementOptions(prefix ?: "",  SpecialChars.COMA,  styleCode)
                }
            }
        }
}

class ListOptions(
    indentWith:String = "",
    separator:String = SpecialChars.NEW_LINE,
    override var indent: Int = 0,
    styleCode: StyleCode? = null,
):StringifyOptions(indentWith, separator, styleCode){
    var header : String = ""
        set(value) {
            if(field.isBlank()){
                field = value
            }
        }
    fun prefixEach(text:String, indentSize:Int = indent){
        indent = indentSize
    }
}

class ElementOptions(
    prefix:String = "",
    separator:String = SpecialChars.COMA,
    styleCode: StyleCode? = null,

):StringifyOptions(prefix, separator,  styleCode){
    constructor(separatorString:String):this(prefix = "", styleCode = null){
        separator = separatorString
    }
    override val indent: Int = 0
}


@PublishedApi
internal fun  stringification(
    receiver: Any?,
    opts: StringifyOptions,
): TextSpan{
    val prefix = opts.normalizedPrefix()
    val useStyleCode = opts.styleCode

    if(receiver == null){
       return  StyledPair("Null")
    }
    return when(receiver){
        is List<*>-> {
            if(opts is ListOptions){
                StyledPair(opts.header)
            }else{
                StyledPair()
            }
        }
        is String -> StyledPair(receiver)
        else -> TextStyler.formatKnown(receiver)
    }
}

fun Any?.stringify(prefix: String? = null, styleCode: StyleCode? = null):TextSpan {
   return  TextStyler.formatKnown(this)
    //stringification(this, StringifyOptions.createForReceiver(this, prefix, styleCode))
}

fun Any?.stringify(styleCode: StyleCode? = null):TextSpan {
    return TextStyler.formatKnown(this)
   // stringification(this, StringifyOptions.createForReceiver(this, "", styleCode))
}


inline fun <reified T: Any> List<T>.stringify(noinline configAction: ListOptions.(T) -> Unit): TextSpan {
    val initialRecord = MutablePair()
    val options = ListOptions()
    forEachIndexed { index, item ->
        if(index == 0){
            configAction.invoke(options, item)
            val header = options.header
            val useStyle =  options.styleCode
            val styledHeader = if(useStyle != null){
                TextStyler.ansi.style(header, useStyle)
            }else{
                header
            }
            initialRecord.append(header, styledHeader)
            initialRecord.append(stringification(item, options))
        }else{
            configAction.invoke(options, item)
            initialRecord.append(stringification(item, options))
        }
    }
    return initialRecord
}


fun <T: Any> T.stringifyTree(
    property: KProperty1<T, Collection<T>>,
    opt: ListOptions? = null
): TextSpan{

//    val useOptions = opt?: StringifyOptions.defaultIndention
//    fun recursiveRun(initialRecord:FormattedText, receiver :T, property: KProperty1<T, Collection<T>>, styleCode: StyleCode?){
//        val records = property.get(receiver)
//        records.forEach { record ->
//            val formatedEntry = TextStyler.formatKnown(record)
//            formatedEntry.styleFormatted(styleCode)
//            //initialRecord.add(formatedEntry)
//            recursiveRun(formatedEntry, record, property, styleCode)
//        }
//    }
//    val rootEntry = TextStyler.formatKnown(this)
//    rootEntry.styleFormatted(useOptions.styleCode)
//    recursiveRun(rootEntry, this, property, useOptions.styleCode)
//    val result = rootEntry.joinSubEntries(useOptions)
    return StyledPair("Refactor")
}


internal fun stringify(vararg parameters : Any?): String{
   val flattened = parameters.flattenVarargs()
   return if(flattened.size > 1){
        flattened.joinToString( SpecialChars.WHITESPACE){ TextStyler.formatKnown(it).styled }
    }else{
        TextStyler.formatKnown(flattened.firstOrNull()).styled
    }
}

internal fun stringifyToSpan(vararg parameters : Any?): TextSpan{
    val flattened = parameters.flattenVarargs()
    return if(flattened.size > 1){
       val spans = flattened.map {
            if(it is TextSpan){
                it
            }else{
                TextStyler.formatKnown(it)
            }
        }
        spans.joinSpans(Postfix(SpecialChars.WHITESPACE))
    }else{
        TextStyler.formatKnown(flattened.firstOrNull())
    }
}

internal fun stringifyToSpan(modifier: TextWrapper, vararg parameters : Any?): TextSpan{
    val flattened = parameters.flattenVarargs()
    return if(flattened.size > 1){
        val spans = flattened.map {
            if(it is TextSpan){
                it
            }else{
                TextStyler.formatKnown(it)
            }
        }
        spans.joinSpans(Postfix(SpecialChars.WHITESPACE))
    }else{
        TextStyler.formatKnown(flattened.firstOrNull())
    }
}

internal fun stringify(modifier: TextWrapper, vararg parameters : Any): String{
    val flattened = parameters.flattenVarargs()
    return if(flattened.size > 1){
        flattened.joinToString(modifier){ TextStyler.formatKnown(it).styled }
    }else{
        modifier.wrap(TextStyler.formatKnown(flattened.firstOrNull()).styled)
    }
}



