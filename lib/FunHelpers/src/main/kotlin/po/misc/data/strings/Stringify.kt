package po.misc.data.strings

import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import kotlin.reflect.KProperty1

sealed interface StringifyOptions {
    val prefix:String
    val styleCode: StyleCode?
    val separator:String
    val indent: Int

    fun normalizedPrefix():String = normalizedPrefix(this)

    class ListOptions(
        var indentWith:String = "",
        override var indent: Int = 0,
        override val styleCode: StyleCode? = null,
    ):StringifyOptions{
        var header : String = ""
        set(value) {
            if(field.isBlank()){
                field = value
            }
        }

        override val prefix:String get() =  indentWith
        override var separator:String = SpecialChars.NEW_LINE

        fun prefixEach(text:String, indentSize:Int = indent){
            indentWith = text
            indent = indentSize
        }

    }
    class ElementOptions(
        override val styleCode: StyleCode? = null,
        override val prefix:String = "",
    ):StringifyOptions{

        constructor(separatorString:String):this(prefix = "", styleCode = null){
            separator = separatorString
        }

        override var separator:String = SpecialChars.COMA
        override val indent: Int = 0
    }

    companion object{

        val defaultIndention: ListOptions = ListOptions(indentWith = " ", indent = 1)
        val noIndention: ListOptions get() =  ListOptions(indentWith = "", indent = 0)

        fun normalizedPrefix(opts: StringifyOptions):String{
            if(opts is ListOptions){
                if(opts.indent == 0 ){
                    return "${opts.prefix.trim()} "
                }
                return opts.prefix
            }else{
                if(opts.prefix.isNotBlank()){
                    return "${opts.prefix.trim()} "
                }
                return ""
            }
        }

        fun elementOptions(prefix:String = "", styleCode: StyleCode? = null): ElementOptions{
            return ElementOptions(styleCode, prefix)
        }
        fun createForReceiver(receiver:Any?, prefix: String?, styleCode: StyleCode?):StringifyOptions{
           return if(receiver is List<*>){
               if(prefix != null){
                   ListOptions(prefix, indent = 1, styleCode)
               }else{
                   ListOptions("", indent = 0, styleCode)
               }
            }else{
               elementOptions(prefix?:"", styleCode)
            }
        }
    }
}

@PublishedApi
internal fun  stringification(
    receiver: Any?,
    opts: StringifyOptions,
): FormattedPair{
    return when(receiver){
        is List<*>-> {
            val formatted = if(opts is StringifyOptions.ListOptions){
                FormattedText(opts.header).styleFormatted(opts.styleCode)
            }else{
                FormattedText()
            }
            for (entry in receiver) {
                formatted.add(TextStyler.formatKnownTypes(entry)).prepend(opts.normalizedPrefix(), opts.styleCode)
            }
            if(opts is StringifyOptions.ListOptions){
                formatted.joinSubEntries(opts)
            }else{
                formatted
            }
        }
        else -> {
            val formatted = TextStyler.formatKnownTypes(receiver)
            formatted.styleFormatted(opts.styleCode)
            formatted.prepend(opts.normalizedPrefix(), opts.styleCode)
            formatted
        }
    }
}

fun Any?.stringify(prefix: String? = null, styleCode: StyleCode? = null):FormattedPair =
        stringification(this, StringifyOptions.createForReceiver(this, prefix, styleCode))

fun Any?.stringify(styleCode: StyleCode? = null):FormattedPair =
    stringification(this, StringifyOptions.createForReceiver(this, "", styleCode))

inline fun <reified T: Any> List<T>.stringify(
    noinline configAction: StringifyOptions.ListOptions.(T) -> Unit
): FormattedPair {
    val initialRecord = FormattedText()
    val options = StringifyOptions.ListOptions()
    forEachIndexed { index, item ->
        if(index == 0){
            configAction.invoke(options, item)
            val header = options.header
            initialRecord.append(header, TextStyler.style(header, options.styleCode), SpecialChars.NEW_LINE)
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
    opt: StringifyOptions.ListOptions? = null
): FormattedPair{
    val useOptions = opt?: StringifyOptions.defaultIndention
    fun recursiveRun(initialRecord:FormattedText, receiver :T, property: KProperty1<T, Collection<T>>, styleCode: StyleCode?){
        val records = property.get(receiver)
        records.forEach { record ->
            val formatedEntry = TextStyler.formatKnownTypes(record)
            formatedEntry.styleFormatted(styleCode)
            initialRecord.add(formatedEntry)
            recursiveRun(formatedEntry, record, property, styleCode)
        }
    }
    val rootEntry = TextStyler.formatKnownTypes(this)
    rootEntry.styleFormatted(useOptions.styleCode)
    recursiveRun(rootEntry, this, property, useOptions.styleCode)
    val result = rootEntry.joinSubEntries(useOptions)
    return result
}







