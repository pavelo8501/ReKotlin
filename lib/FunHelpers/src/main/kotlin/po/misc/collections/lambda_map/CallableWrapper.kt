package po.misc.collections.lambda_map

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.misc.context.component.Component
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.handling.Suspended
import kotlin.coroutines.CoroutineContext


interface CallableWrapper<T: Any, R>{
    fun invoke(value: T):R
    suspend fun invoke(value: T, suspending: Suspended):R
}

class Lambda<T: Any, R>(
    private val lambda: Function1<T, R>
):CallableWrapper<T, R>{
    override fun invoke(value: T):R{
        return lambda.invoke(value)
    }
    override suspend fun invoke(value: T, suspending: Suspended):R{
        return invoke(value)
    }
}

fun <T: Any, R> Function1<T, R>.toCallable():Lambda<T, R>{
    return Lambda(this)
}

class LambdaWithReceiver<H: Any, T: Any, R>(
    val receiver:H,
    private val lambda: Function2<H, T, R>
):CallableWrapper<T, R>{
    override fun invoke(value: T):R{
        return lambda.invoke(receiver, value)
    }
    override suspend fun invoke(value: T, suspending: Suspended):R{
        return invoke(value)
    }
}

fun <H: Any, T: Any, R> Function2<H, T, R>.toCallable(receiver:H):LambdaWithReceiver<H, T, R>{
    return LambdaWithReceiver(receiver, this)
}

class SuspendingLambda<T: Any, R>(
    private val lambda: suspend (T)->R
):CallableWrapper<T, R>{

    var receiversContext: CoroutineContext? = null

    constructor(context: CoroutineContext, lambda: suspend (T)->R):this(lambda){
        receiversContext = context
    }

    override fun invoke(value: T):R{
        val errorMsg = "Invoke call  in SuspendingLambda wrapping class, not holding non suspended lambda"
        errorMsg.output(Colour.Yellow)
        return receiversContext?.let {context->
            runBlocking {
                withContext(context){
                    invoke(value,  Suspended)
                }
            }
        }?:run {
            throw IllegalArgumentException(errorMsg)
        }
    }

    override suspend fun invoke(value: T, suspending: Suspended):R{
        return lambda.invoke(value)
    }
}

fun <T: Any, R> Component.toCallable(function: suspend (T)->R):SuspendingLambda<T, R>{
    return SuspendingLambda(function)
}

class SuspendingLambdaWithReceiver<H: Any, T: Any, R>(
    val receiver:H,
    private val lambda: suspend H.(T)->R
):CallableWrapper<T, R>{

    var receiversContext: CoroutineContext? = null

    constructor(context: CoroutineContext, receiver:H,  lambda: suspend H.(T)->R):this(receiver, lambda){
        receiversContext = context
    }

    override fun invoke(value: T):R {
        val errorMsg = "Invoke call  in SuspendingLambda wrapping class, not holding non suspended lambda"
        errorMsg.output(Colour.Yellow)
        return receiversContext?.let { context ->
            runBlocking {
                withContext(context) {
                    invoke(value, Suspended)
                }
            }
        } ?: run {
            throw IllegalArgumentException(errorMsg)
        }
    }

    override suspend fun invoke(value: T, suspending: Suspended):R{
        return lambda.invoke(receiver, value)
    }
}

fun <H: Component, T: Any, R>  H.toCallable(function: suspend H.(T)->R):SuspendingLambdaWithReceiver<H, T, R>{
    return SuspendingLambdaWithReceiver(this, function)
}