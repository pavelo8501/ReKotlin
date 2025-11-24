package po.misc.debugging

import po.misc.context.CTX
import po.misc.context.tracable.TraceableContext

import po.misc.types.k_class.simpleOrAnon



fun TraceableContext.identityData(): DebugFrameData{
    val kClass = this::class
    val simpleName = kClass.simpleOrAnon
    var hash: Int = 0
   return when(this){
        is CTX -> {
            DebugFrameData(this)
        }
        is TraceableContext->{
            hash = this.hashCode()
            DebugFrameData(simpleName, numericId = hash.toLong())
        }

    }
}
