package po.misc.exceptions

import po.misc.exceptions.TraceOptions.TraceType
import kotlin.reflect.KFunction


sealed interface TraceOptions{
    enum class TraceType{ Default, CallSite }
    val traceType:TraceType
}

open class TraceCallSite(var methodName: String): TraceOptions{

    constructor(function : KFunction<*>):this(function.name){
        kFunction = function
    }

    override val traceType:TraceType = TraceType.CallSite
    var kFunction : KFunction<*>? = null

    fun provideMethodName(name: String): TraceCallSite{
        methodName = name
        return this
    }

}
