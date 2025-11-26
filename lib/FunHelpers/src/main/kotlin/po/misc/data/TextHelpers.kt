package po.misc.data

import po.misc.data.strings.FormatedEntry
import po.misc.data.strings.stringify
import po.misc.data.strings.stringifyList

inline fun <T: Any, R> T.messageAssembler(vararg parts: Any,  block: T.(FormatedEntry) -> R):R{
   val asList = parts.toList()
    var resultEntry : FormatedEntry? = null
    if(asList.size == 1){
        val first = asList.first()
        resultEntry = if(first is Array<*>){
            first.stringifyList()
        }else{
            first.stringify()
        }
    }else{
        resultEntry =  asList.stringify()
    }
   return block.invoke(this, resultEntry)
}


/**
 * Converts a camelCase / PascalCase / snake_case identifier into
 * a human-friendly label.
 *
 * Examples:
 *  - "methodName" -> "Method name"
 *  - "simpleClassName" -> "Simple class name"
 *  - "URLPath" -> "URL path"
 *  - "xmlHTTPResponse" -> "Xml HTTP response"
 *  - "full_name" -> "Full name"
 */
fun String.toDisplayName(): String {

    if (this.isEmpty()) return this
    val cleaned = this.replace('_', ' ')
    val words = cleaned.split(" ").flatMap { chunk ->
        chunk.split("(?<!^)(?=[A-Z])".toRegex())
    }
    if (words.isEmpty()) return this

    return words.mapIndexed { index, w ->
        when {
            w.length > 1 && w.all { it.isUpperCase() } -> w
            index == 0 -> w.replaceFirstChar { it.uppercase() }
            else -> w.lowercase()
        }
    }.joinToString(" ")
}
