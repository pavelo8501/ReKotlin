package po.misc.debugging.stack_tracer

import po.misc.types.k_function.receiverClasName
import kotlin.reflect.KFunction


sealed interface TraceOptions{

    val printImmediately: Boolean
    val beforeThisMethod : Boolean
    val methodName:String?

    object Default: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = false
        override val methodName:String? = null
    }

    object ThisMethod: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = false
        override var methodName:String = ""
    }

    object PreviousMethod: TraceOptions{
        override val printImmediately: Boolean = true
        override val beforeThisMethod : Boolean = true
        override var methodName:String = ""
    }

    open class Method(override val methodName:String) : TraceOptions{
        override var printImmediately: Boolean = false
        override var beforeThisMethod: Boolean = true
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

