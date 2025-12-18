package po.misc.callbacks.context_signal

import po.misc.callbacks.CallableEventBase
import po.misc.callbacks.common.ListenerResult
import po.misc.collections.lambda_map.Lambda
import po.misc.collections.lambda_map.toCallable
import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.Suspended
import po.misc.functions.Sync
import po.misc.types.token.TypeToken


class ContextSignal<T, T1, R>(
     val type: TypeToken<T>,
     val paramType: TypeToken<T1>,
     val resultType: TypeToken<R>,
):  CallableEventBase<T, T1, R>() {

    private val unsupportedMsg = "Unsupported parameter type of Unit. Signal demands a parameter T1 of class ${paramType.kClass}"

    var signal:Boolean = false
    private set

    var suspended:Boolean = false
    private set

    var signalName:String = "ContextSignal"
        set(value) {
            field = value
            eventName = value
        }

    internal fun applyOptions(opt: CTXSignalOpt):CallableEventBase<T, T1, R>{
        signalName = opt.name
        return this
    }

    override fun listenersApplied(lambdaType: LambdaType) {
        when(lambdaType) {
            is Sync -> signal = true
            is Suspended -> suspended = true
        }
    }

    fun onSignal(
        listener: TraceableContext,
        options: LambdaOptions = LambdaOptions.Listen,
        callback: T.(T1) -> R
    ) {
        signal = true
        listenersMap[listener] =  callback.toCallable(options)
    }

    fun onSignal(
        options: LambdaOptions = LambdaOptions.Listen,
        callback: T.(T1) -> R
    ) {
        signal = true
        listenersMap[this] =  callback.toCallable(options)
    }

    @Suppress("UNCHECKED_CAST")
    fun trigger(value: T): List<ListenerResult<R>>{
        if(paramType.kClass == Unit::class){
           return super.trigger(value, Unit as T1)
        }
        throw IllegalStateException(unsupportedMsg)
    }

//    companion object{
//        inline operator fun <reified T, reified T1, reified R> invoke():ContextSignal<T, T1, R>{
//           return ContextSignal(TypeToken<T>(), TypeToken<T1>(), TypeToken<R>())
//        }
//    }
}

