package po.misc.debugging.stack_tracer

import po.misc.data.pretty_print.parts.RowID
import po.misc.debugging.stack_tracer.TraceOptions.TraceType
import po.misc.types.k_function.receiverClasName
import kotlin.reflect.KFunction


sealed interface TraceOptions{
    enum class TraceType : RowID{ Default, CallSite, Methods }
    val type:TraceType
    val printImmediately: Boolean

    object Default: TraceOptions{
        override val type:TraceType = TraceType.Default
        override val printImmediately: Boolean = true
    }
}

open class CallSite(
    var methodName: String,
    val className: String? = null
) : TraceOptions{

    constructor(function : KFunction<*>):this(function.receiverClasName, function.name)
    constructor(className: String, function : KFunction<*>):this(className, function.name)
    override val type:TraceType = TraceType.CallSite

    override var printImmediately: Boolean = true

    fun provideMethodName(name: String): CallSite{
        methodName = name
        return this
    }
}

open class Methods(val methodName:String) : TraceOptions{

    override val type:TraceType = TraceType.Methods
    override var printImmediately: Boolean = false
}
