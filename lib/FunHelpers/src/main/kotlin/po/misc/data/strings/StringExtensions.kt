package po.misc.data.strings

import po.misc.data.Postfix
import po.misc.data.Prefix
import po.misc.data.Separator
import po.misc.data.StringModifyParams
import po.misc.data.TextWrapper
import kotlin.collections.joinTo


fun <T> Iterable<T>.joinToString(textWrapper: TextWrapper, limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    var separator =  ""
    var prefix = ""
    var postfix = ""
    when(textWrapper){
        is StringModifyParams ->{
            separator =  textWrapper.separator.value
            prefix = textWrapper.prefix.value
            postfix = textWrapper.postfix.value
        }
        is Prefix -> prefix = textWrapper.value
        is Postfix -> postfix = textWrapper.value
    }
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}