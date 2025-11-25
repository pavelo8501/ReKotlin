package po.misc.exceptions

import po.misc.exceptions.TraceOptions.TraceType
import kotlin.reflect.KFunction


sealed interface TraceOptions{

    enum class TraceType{ Default, CallSite }

    val traceType:TraceType
}

open class TraceCallSite(var methodName: String): TraceOptions{

    override val traceType:TraceType = TraceType.CallSite

    constructor(function : KFunction<*>):this(function.name)

    fun provideMethodName(name: String): TraceCallSite{
        methodName = name
        return this
    }
}
