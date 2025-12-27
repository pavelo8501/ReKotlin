package po.misc.data.strings

import po.misc.data.isUnset
import po.misc.data.styles.SpecialChars

//fun String?.ifNotBlank(block: (String)-> String): String{
//    return if(!this.isNullOrBlank()){
//        block.invoke(this)
//    }else{
//        SpecialChars.EMPTY
//    }
//}


fun <T> Iterable<T>.joinToStringNotBlank(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    val filtered = filter { it.isUnset }
    return filtered.joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}
