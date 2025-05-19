package po.misc.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext





@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, R> T.RunAsync(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend CoroutineScope.() -> R
): R = withContext(this.coroutineContext + dispatcher) {
    block()
}

@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, R> T.RunConcurrent(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend CoroutineScope.() -> R
): R {
    return  CoroutineScope(this.coroutineContext + dispatcher).async(start = CoroutineStart.UNDISPATCHED) {
        block()
    }.await()
}

data class LauncherPayload<T, P, R>(
    val lambda : T.(P)->R,
    val receiver: T,
    val parameter: P
)

@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, REC, P, R> T.RunAsync(
    payload: LauncherPayload<REC, P, R>,
    block: suspend CoroutineScope.() -> R
): R = withContext(this.coroutineContext) {
    block()
}




//
//suspend  fun <T: CoroutineHolder,  R>  T.RunAsync3(
//    block: suspend CoroutineScope.()-> R
//):R{
//    return CoroutineScope(this.coroutineContext).async{
//            block.invoke(this)
//    }.await()
//}
//
//
//@Suppress("FunctionName")
//fun <T, R> RunAsync2(
//    receiver : T,
//    context: CoroutineHolder,
//    block: suspend T.()-> R)
//:R{
//   val myThreadLocal = ThreadLocal<CoroutineHolder>()
//   return runBlocking {
//         CoroutineScope(context.coroutineContext + myThreadLocal.asContextElement(context)).async {
//            val compositeContext = coroutineContext
//            block.invoke(receiver)
//        }.await()
//    }
//}
//
//@Suppress("FunctionName")
//suspend fun <T, R> RunAsync(
//    receiver : T,
//    block: suspend T.()-> R)
//:R{
//    val myThreadLocal = ThreadLocal<CoroutineHolder>()
//    val thisContext = coroutineContext
//   return CoroutineScope(context.coroutineContext + myThreadLocal.asContextElement() + thisContext).async {
//        val compositeContext = coroutineContext
//        block.invoke(receiver)
//    }.await()
//}
//
//@Suppress("FunctionName")
//suspend fun <T, R> RunContext(
//    receiver: T,
//    context: CoroutineHolder,
//    block: suspend T.() -> R
//): R {
//    val myThreadLocal = ThreadLocal<CoroutineHolder>()
//  return withContext(myThreadLocal.asContextElement()+ context.coroutineContext) {
//      val compositeContext = coroutineContext
//      block(receiver)
//    }
//}