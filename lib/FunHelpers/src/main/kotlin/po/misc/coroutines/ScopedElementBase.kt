package po.misc.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.data.output.output
import po.misc.data.styles.Colour
import kotlin.coroutines.CoroutineContext


abstract class ScopedElementBase<T> (
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
): CTX,  CoroutineContext.Element  where T: ScopedElementBase<T>, T : CTX{

    abstract override val identity: CTXIdentity<T>
    abstract override val key: CoroutineContext.Key<T>

    open val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + this + dispatcher + CoroutineName(identity.identifiedByName))
    }

    val coroutineContext: CoroutineContext get() = scope.coroutineContext

    open fun close() {
        scope.cancel()
    }
}

suspend  fun <T, R> T.runElementAwait(
    block: suspend CoroutineScope.(T) -> R
): R where  T: ScopedElementBase<T>, T : CTX  {
    return scope.async(start = CoroutineStart.UNDISPATCHED) {
        val receiver = this@runElementAwait
        try {
            block(receiver)
        }catch (th: Throwable){
            th.output()
            "Caught in runElementAwait of ScopedElementBase".output(Colour.BlueBright)
            throw th
        }

    }.await()
}

fun <T> T.runElementAsync(
    block: suspend CoroutineScope.(T) -> Unit
): Job  where  T: ScopedElementBase<T>, T : CTX {
    return scope.launch(start = CoroutineStart.UNDISPATCHED) {
        val receiver = this@runElementAsync
        try {
            block(receiver)
        }catch (th: Throwable){
            th.output()
            "Caught in runElementAsync of ScopedElementBase".output(Colour.BlueBright)
            throw th
        }

    }
}

