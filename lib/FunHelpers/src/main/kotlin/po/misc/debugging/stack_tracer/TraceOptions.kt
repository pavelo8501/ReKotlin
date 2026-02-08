package po.misc.debugging.stack_tracer

import po.misc.callbacks.callable.CallableMeta
import po.misc.types.k_function.receiverClasName
import kotlin.reflect.KFunction


sealed interface TraceOptions{

    enum class Lookup { Unknown,  ThisMethod, BeforeThis }

    val printImmediately: Boolean
    val beforeThisMethod : Boolean
    val methodName:String
    val lookup: Lookup get() =  Lookup.Unknown


    object Default: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = false
        override val methodName:String = ""
    }

    object ThisMethod: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = false
        override var methodName:String = ""
        override val lookup: Lookup = Lookup.ThisMethod
    }

    object PreviousMethod: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = true
        override var methodName:String = ""
        override val lookup: Lookup = Lookup.BeforeThis
    }

    open class Method(
        override var methodName:String,
        override val lookup: Lookup = Lookup.ThisMethod,
        override val printImmediately: Boolean = true
    ):TraceOptions{
        override var beforeThisMethod : Boolean = true
        var className:String? = null
        var javaName:String? = null

        constructor(
            callableMeta: CallableMeta,
            printImmediately: Boolean = true,
        ):this(callableMeta.functionName, Lookup.BeforeThis, printImmediately){
            beforeThisMethod = true
            className = callableMeta.receiverName
            javaName = callableMeta.javaName
        }
    }

}

open class CallSite(
    override var methodName: String,
    val className: String? = null
) : TraceOptions {

    constructor(methodName: String, beforeThisMethod : Boolean):this(methodName){
        this.beforeThisMethod = beforeThisMethod
    }
    constructor(function : KFunction<*>):this(function.receiverClasName, function.name)
    constructor(className: String, function : KFunction<*>):this(className, function.name)
    override var beforeThisMethod : Boolean = false
    override var printImmediately: Boolean = true

    fun provideMethodName(name: String): CallSite{
        methodName = name
        return this
    }
}

