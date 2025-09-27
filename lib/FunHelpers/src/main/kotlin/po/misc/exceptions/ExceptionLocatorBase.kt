package po.misc.exceptions

import po.misc.context.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.models.ExceptionTrace
import po.misc.types.helpers.simpleOrNan
import po.misc.types.safeCast
import kotlin.reflect.KClass

data class HelperPackage(
    val name: String
)


interface ThrowableLambda<TH: Throwable,  T: Any>{
    val data: T
    val lambda: suspend (TH) -> Unit

}

data class ThrowableContainer<TH: Throwable, T: Any>(
    override val data: T,
    override val lambda: suspend (TH) -> Unit
):ThrowableLambda<TH, T>




abstract class ExceptionLocatorBase{

    abstract val helperPackages: MutableList<HelperPackage>
    internal  val traceMap = mutableMapOf<KClass<*>, MutableList<ExceptionTrace>>()

    @PublishedApi
    internal fun register(exceptionTrace: ExceptionTrace):ExceptionTrace{
        val resultingList = mutableListOf<ExceptionTrace>()
        traceMap[exceptionTrace.kClass]?.let {
            resultingList.addAll(it)
        }
        val existent =  traceMap.getOrPut(exceptionTrace.kClass){
            mutableListOf(exceptionTrace)
        }
        existent.add(exceptionTrace)
        return exceptionTrace
    }

    @PublishedApi
    internal val exceptionBuilderRegistry: MutableMap<KClass<*>, (String) -> Throwable> = mutableMapOf()

    @PublishedApi
    internal val handlers: MutableMap<KClass<out Throwable>, (Throwable) -> Unit> = mutableMapOf()

    @PublishedApi
    internal val suspendedHandlers: MutableMap<KClass<out Throwable>, ThrowableLambda<Throwable, *>> = mutableMapOf()


    inline fun <reified TH> registerExceptionBuilder(
       noinline provider: (String)-> TH
    ) where  TH: Throwable, TH: TrackableException{
        exceptionBuilderRegistry[TH::class] = provider
    }

    fun raiseManagedException(
        context: TraceableContext,
        message: String,
        traceProvider: ((ExceptionTrace)-> Unit)?
    ): ExceptionTrace {

        val managed =  ManagedException(context = context, message = message)
        val trace =  register(managed.exceptionTrace)
        traceProvider?.invoke(trace)
        throw managed
    }

    inline fun <reified TH> raiseException(
        context: TraceableContext,
        message: String,
    ): ExceptionTrace  where  TH: Throwable, TH: TrackableException {
        val exception = exceptionBuilderRegistry[TH::class]?.invoke(message) ?:run {
            ManagedException(context = context, message = message)
        }
       when(exception){
            is TrackableException ->  register(exception.exceptionTrace)
            is Throwable ->  exception.metaFrameTrace(context)
            else -> {
                val msg = "ExceptionLocatorBase on raiseException call created exception of unknown type"
                throw IllegalArgumentException(msg)
            }
        }
        throw exception
    }

    inline fun <reified TH> raiseException(
        context: TraceableContext,
        message: String,
        crossinline traceProvider: (ExceptionTrace)-> Unit
    ): ExceptionTrace  where  TH: Throwable, TH: TrackableException {

        val exception = exceptionBuilderRegistry[TH::class]?.invoke(message) ?:run {
            ManagedException(context = context, message = message)
        }

        val trace =  when(exception){
            is TrackableException ->  register(exception.exceptionTrace)
            is Throwable ->  exception.metaFrameTrace(context)
            else -> {
                val msg = "ExceptionLocatorBase on raiseException call created exception of unknown type"
                throw IllegalArgumentException(msg)
            }
        }
        traceProvider.invoke(trace)
        throw exception
    }



    inline fun <reified TH: Throwable> registerHandler(noinline handler: (TH)-> Unit): Boolean {

        val castedHandler = handler.safeCast<(Throwable)-> Unit>()
        return castedHandler?.let {
            handlers[TH::class] = it
            "Handler registered for (${TH::class.simpleOrNan()})->Unit".output(Colour.GreenBright)
            true
        }?:run {
            "Cast of handler (${TH::class.simpleOrNan()})->Unit  to (Throwable)->Unit failed".output(Colour.YellowBright)
            false
        }
    }



    inline fun <reified TH: Throwable, reified T: Any> registerHandler(
        handler: ThrowableLambda<TH, T>
    ): Boolean {

       return handler.safeCast<ThrowableLambda<Throwable, *>>()?.let { casted->
            suspendedHandlers[TH::class] = casted
           "Handler registered for ThrowableLambda<${TH::class.simpleOrNan()}, ${T::class}>".output(Colour.GreenBright)
            true
        }?:run {
           "Cast failure for ThrowableLambda<${TH::class.simpleOrNan()}, ${T::class}>".output(Colour.YellowBright)
            false
        }
    }


    fun <TH: Throwable> handle(throwable:TH){
        val handlerFound =  handlers[throwable::class]
        if(handlerFound != null){
            "Handler for   ${throwable::class.simpleOrNan()} found. Invoking".output(Colour.GreenBright)
            handlerFound.invoke(throwable)
        }else{
            "No handler registered for  ${throwable::class.simpleOrNan()}. Rethrowing".output(Colour.YellowBright)
            throw throwable
        }
    }

    suspend fun  <TH: Throwable> handleSuspending(throwable:TH){
        val handlerFound =  suspendedHandlers[throwable::class]
        if(handlerFound != null){
            "Handler for  suspended lambda  ${throwable::class.simpleOrNan()} found. Invoking".output(Colour.GreenBright)
            handlerFound.lambda.invoke(throwable)
        }else{
            "No handler registered for suspended lambda  ${throwable::class.simpleOrNan()}. Rethrowing".output(Colour.YellowBright)
            throw throwable
        }
    }

    fun register(throwable: Throwable){
        throwable.throwableToText().output()
    }

}