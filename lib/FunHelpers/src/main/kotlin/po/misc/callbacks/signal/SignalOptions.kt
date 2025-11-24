package po.misc.callbacks.signal

import po.misc.context.tracable.TraceableContext
import po.misc.debugging.ClassResolver

data class SignalOptions(
    val name: String,
    val host: TraceableContext? = null
){

    val hostName: String get() {
       return if(host != null){
            ClassResolver.instanceName(host)
        }else{
            ""
        }
    }

}