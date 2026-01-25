package po.misc.data.strings

import po.misc.data.output.output
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.TextSpan
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
    return when(receiver){
        is List<*>-> {
            val formatted = if(opts is ListOptions){
                FormattedText(opts.header).styleFormatted(opts.styleCode)
            }else{
                FormattedText()
            }
            for (entry in receiver) {
                TextStyler.formatKnownTypes(entry)

                formatted.prepend(opts.normalizedPrefix(), opts.styleCode)
                formatted
            }
            if(opts is ListOptions){
                formatted.joinSubEntries(opts)
            }else{
                formatted
            }
        }
        is String -> {
            val formattedText = if(useStyleCode != null ){
                if(opts.preserveAnsi){
                    TextStyler.applyStyle(receiver, useStyleCode)
                }else{
                    TextStyler.style(receiver, useStyleCode)
                }
            }else{
                receiver
            }
            FormattedText(receiver, formattedText)
        }
        else -> {
            val formatted = TextStyler.formatKnownTypes(receiver)
            formatted.styleFormatted(opts.styleCode)
            if(prefix.isNotBlank()){
                formatted.prepend(prefix, opts.styleCode)
            }

            formatted
        }
    }
}

fun Any?.stringify(prefix: String? = null, styleCode: StyleCode? = null):TextSpan =
        stringification(this, StringifyOptions.createForReceiver(this, prefix, styleCode))

fun Any?.stringify(styleCode: StyleCode? = null):TextSpan =
    stringification(this, StringifyOptions.createForReceiver(this, "", styleCode))

inline fun <reified T: Any> List<T>.stringify(
    noinline configAction: ListOptions.(T) -> Unit

): TextSpan {
    val initialRecord = FormattedText()
    val options = ListOptions()
    forEachIndexed { index, item ->
        if(index == 0){
            configAction.invoke(options, item)
            val header = options.header
            val useStyle =  options.styleCode
            val styledHeader = if(useStyle != null){
                TextStyler.style(header, useStyle)
            }else{
                header
            }
            initialRecord.append(header, styledHeader, SpecialChars.NEW_LINE)
            initialRecord.append(stringification(item, options), options)
        }else{
            configAction.invoke(options, item)
            initialRecord.append(stringification(item, options),options)
        }
    }
    return initialRecord
}


fun <T: Any> T.stringifyTree(
    property: KProperty1<T, Collection<T>>,
    opt: ListOptions? = null
): TextSpan{

    val useOptions = opt?: StringifyOptions.defaultIndention
    fun recursiveRun(initialRecord:FormattedText, receiver :T, property: KProperty1<T, Collection<T>>, styleCode: StyleCode?){
        val records = property.get(receiver)
        records.forEach { record ->
            val formatedEntry = TextStyler.formatKnownTypes(record)
            formatedEntry.styleFormatted(styleCode)
            //initialRecord.add(formatedEntry)
            recursiveRun(formatedEntry, record, property, styleCode)
        }
    }
    val rootEntry = TextStyler.formatKnownTypes(this)
    rootEntry.styleFormatted(useOptions.styleCode)
    recursiveRun(rootEntry, this, property, useOptions.styleCode)
    val result = rootEntry.joinSubEntries(useOptions)
    return result
}

