package po.misc.context

import po.misc.types.helpers.simpleOrNan


data class ContextInfo(
   val  contextName: String,
   val  contextHash: Int,
   val  uuidSting: String? = null
)

interface ContextualHelper {
    fun callingName(callingContext: Any): ContextInfo{
       return when(callingContext){
            is CTX -> {
                ContextInfo(
                    callingContext.identifiedByName,
                    callingContext.identity.hashCode(),
                    callingContext.identity.numericId.toString())
            }
            else ->{
                ContextInfo(
                    callingContext::class.simpleOrNan(),
                    callingContext.hashCode(),
                    null
                )
            }
        }
    }

}