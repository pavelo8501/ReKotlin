package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.TraceOptions.TraceType
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.k_function.receiverClasName
import po.misc.types.k_function.receiverClass
import kotlin.reflect.KFunction


sealed interface TraceOptions{
    enum class TraceType{ Default, CallSite }
    val traceType:TraceType
    val className: String
    val methodName: String?
}

open class TraceCallSite(override var className: String,  override var methodName: String?): TraceOptions{

    constructor(function : KFunction<*>):this(function.receiverClasName, function.name)

    constructor(className: String, function : KFunction<*>):this(className, function.name){
        kFunction = function
    }

    override val traceType:TraceType = TraceType.CallSite

    var kFunction : KFunction<*>? = null

    fun provideMethodName(name: String): TraceCallSite{
        methodName = name
        return this
    }

}
