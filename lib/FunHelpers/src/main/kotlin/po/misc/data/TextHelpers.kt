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