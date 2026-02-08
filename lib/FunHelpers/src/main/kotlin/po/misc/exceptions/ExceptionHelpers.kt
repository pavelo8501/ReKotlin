package po.misc.exceptions

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.extractTrace
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


fun throwManaged(message: String, callingContext: Any): Nothing{
    val methodName = "throwManaged"
    val payload =  ManagedPayload(message, methodName, callingContext)
    throw ManagedException(payload)
}


@PublishedApi
internal inline fun <reified T: Throwable> createThrowable(msg: Any): Throwable{
   val message = if(msg is PrettyPrint) {
        msg.formattedString
    }else{
        msg.toString()
    }
   return when(T::class){
        IllegalStateException::class -> IllegalStateException(message)
        IllegalArgumentException::class -> IllegalArgumentException(message)
        ClassCastException::class -> ClassCastException(message)
        else -> Throwable(message)
    }
}

@JvmName("errorReifiedVariant")
inline fun <reified TH: Throwable> error(message: Any, traceOption: TraceOptions): Nothing{
    if(traceOption.printImmediately){
        val illegal = createThrowable<TH>(message)
        illegal.extractTrace(traceOption).output()
        throw illegal
    }else{
        val tracer = Tracer(traceOption).trace.formattedString
        throw  createThrowable<TH>("$message: $tracer")
    }
}

fun error(message: Any, traceOption: TraceOptions): Nothing =
    error<IllegalStateException>(message, traceOption)

fun error(message: String, data: PrettyPrint, traceOption: TraceOptions = TraceOptions.Default): Nothing =
    error<IllegalStateException>("$message: ${data.formattedString}", traceOption)


@OptIn(ExperimentalContracts::class)
inline fun <T : Any> checkNotNull(value: T?, traceOption: TraceOptions, lazyMessage: () -> String): T {
    contract {
        returns() implies (value != null)
    }
    if (value == null) {
        val message = lazyMessage()
        error(message, traceOption)
    } else {
        return value
    }
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> checkNotNull(value: T?,  traceOption: TraceOptions): T {
    contract {
        returns() implies (value != null)
    }
    return checkNotNull(value, traceOption) { "Required value was null." }
}



@OptIn(ExperimentalContracts::class)
fun <T> T.checkIfTrue(predicate: Boolean,  failAction: (T)-> Unit):T{
    contract {
        returns() implies predicate
    }
    if (!predicate) {
        failAction.invoke(this)
    }
    return this
}
