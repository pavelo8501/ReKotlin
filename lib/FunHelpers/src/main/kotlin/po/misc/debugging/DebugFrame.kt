package po.misc.debugging

import po.misc.context.CTX
import po.misc.exceptions.models.StackFrameMeta
import po.misc.types.helpers.simpleOrNan

data class DebugFrame(
    val frameMeta: List<StackFrameMeta>,
    val inContext: Any,
){
    val contextName: String
    val completeName: String

    init {
        if(inContext is CTX){
            contextName = inContext.contextName
            completeName = inContext.identifiedByName
        }else{
            contextName = inContext::class.simpleOrNan()
            completeName = inContext::class.qualifiedName?:"N/A"
        }
    }

}
