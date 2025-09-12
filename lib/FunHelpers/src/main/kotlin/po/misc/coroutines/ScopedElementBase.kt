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
import kotlin.coroutines.CoroutineContext


abstract class ScopedElementBase<T> (
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
): CTX, CoroutineContext.Element  where T: ScopedElementBase<T>{

    abstract override val identity: CTXIdentity<T>
    abstract override val key: CoroutineContext.Key<T>

    open val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + this + dispatcher + CoroutineName(identifiedByName))

    val coroutineContext: CoroutineContext get() = scope.coroutineContext

    open fun close() {
        scope.cancel()
    }
}

suspend  fun <T: ScopedElementBase<T>, R> T.runElementAwait(
    block: suspend CoroutineScope.() -> R
): R {
    return scope.async(start = CoroutineStart.UNDISPATCHED) {
        block()
    }.await()
}

fun <T: ScopedElementBase<T>> T.runElementAsync(
    block: suspend CoroutineScope.() -> Unit
): Job{
    return scope.launch(start = CoroutineStart.UNDISPATCHED) {
        block()
    }
}



//val CoroutineContext.scopedElement get() = this[ScopedElementBase]
//
//fun CoroutineContext.scopedElement(): Any? {
//
//    val element = this[ScopedElementBase]
//    return element?.safeCast<Any>()
//}
//
//inline  fun <reified T: ScopedElementBase<T>> CoroutineContext.scopedElementInContext():T? {
//    try {
//       val asCompanion = T::class.companionObjectInstance  as T
//        val element =  this[asCompanion.key]
//        return element
//    }catch (th: Throwable){
//        th.throwableToText().output()
//    }
//    return null
//}

