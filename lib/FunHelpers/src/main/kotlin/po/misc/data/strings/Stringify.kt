package po.misc.data.strings

import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import kotlin.reflect.KProperty1


internal fun <T, R> T?.doIf(fallback:R,  block: (T) ->R):R{
    return  if(this != null){
        block.invoke(this)
    }else{
        fallback
    }
}

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
        override val prefix:String = "",
        override val styleCode: StyleCode? = null,
    ):StringifyOptions{
        override val separator:String = SpecialChars.COMA
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
            return ElementOptions(prefix, styleCode)
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
                val header = opts.header
                FormattedText(header).style(opts.styleCode)
            }else{
                FormattedText()
            }
            for (entry in receiver) {

                formatted.add(StringFormatter.formatKnownTypes(entry)).applyPrefix(opts.normalizedPrefix(), opts.styleCode)
            }
            formatted
        }
        else -> StringFormatter.formatKnownTypes(receiver).applyPrefix(opts.normalizedPrefix()).style(opts.styleCode)
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
            initialRecord.applyText(header, StringFormatter.style(header, options.styleCode), SpecialChars.NEW_LINE)
            initialRecord.applyFormatted(stringification(item, options), options)
        }else{
            configAction.invoke(options, item)
            initialRecord.applyFormatted(stringification(item, options),options)
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
            val formatedEntry = StringFormatter.formatKnownTypes(record)
            formatedEntry.style(styleCode)
            initialRecord.add(formatedEntry)
            recursiveRun(formatedEntry, record, property, styleCode)
        }
    }
    val rootEntry = StringFormatter.formatKnownTypes(this)
    rootEntry.style(useOptions.styleCode)
    recursiveRun(rootEntry, this, property, useOptions.styleCode)
    val result = rootEntry.joinSubEntries(useOptions)
    return result
}







