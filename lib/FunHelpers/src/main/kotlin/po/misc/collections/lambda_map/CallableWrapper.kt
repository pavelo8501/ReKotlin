package po.misc.collections.lambda_map

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.managedException
import po.misc.functions.CallableOptions
import po.misc.functions.LambdaOptions
import po.misc.functions.SuspendedOptions
import po.misc.types.getOrThrow
import po.misc.types.k_function.lambdaName
import kotlin.coroutines.CoroutineContext


sealed interface SuspendedWrapper<T, T1, R>: CallableWrapper<T, T1, R>{
    override val isSuspended: Boolean get() = true
    suspend fun invoke(value: T, param: T1):R
}

sealed interface CallableWrapper<in T, in V, out R>: Component  {
    val lambdaName: String
    val options: CallableOptions
    val isSuspended: Boolean
}
sealed interface LambdaWrapper<in T, in V, out R>: CallableWrapper<T, V, R> {
    override val isSuspended: Boolean get() = false
    fun call(value: T, param: V):R
}


class Lambda<T, R>(
    override val options: LambdaOptions = LambdaOptions.Listen,
    private val lambda:(T) -> R
):LambdaWrapper<T, Unit, R>{

    constructor(lambda: (T) -> R): this(LambdaOptions.Listen, lambda)

    override var lambdaName: String = lambda::class.lambdaName

    init {
        when(options){
            is LambdaOptions.NamedListen-> lambdaName = options.name
            is LambdaOptions.NamedPromise-> lambdaName = options.name
            else -> {}
        }
    }
    override val isSuspended: Boolean = false
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })
    override fun call(value: T, param: Unit) : R = invoke(value, param)
    fun invoke(value: T, param: Unit) : R = lambda.invoke(value)
}

class LambdaWithReceiver<H, T, R>(
    override val options: LambdaOptions = LambdaOptions.Listen,
    private val lambda: H.(T) -> R
):LambdaWrapper<H, T, R> {

    constructor(receiver: H, options: LambdaOptions, lambda: H.(T) -> R): this(options, lambda){
        receiverBacking = receiver
    }
    constructor(receiver: H, lambda: H.(T) -> R): this(lambda = lambda){
        receiverBacking = receiver
    }
    override var lambdaName: String = lambda::class.lambdaName
    init {
        when(options){
            is LambdaOptions.NamedListen-> lambdaName = options.name
            is LambdaOptions.NamedPromise-> lambdaName = options.name
            else -> {}
        }
    }
    var receiverBacking : H? = null
    val receiver: H get() {
        return receiverBacking.getOrThrow()
    }
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })
    override fun call(value: H, param:T) : R = invoke(value, param)
    fun invoke(value: H, param:T): R =  lambda.invoke(value, param)
}

class SuspendingLambda<T, R>(
    override val options: SuspendedOptions = SuspendedOptions.Listen,
    private val lambda: suspend (T) -> R
): SuspendedWrapper<T, Unit,  R>{

    override val isSuspended: Boolean = true
    override val lambdaName: String = options.name?: lambda::class.lambdaName
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })
    private val subjectInvoke = "Invoke call"
    var receiversContext: CoroutineContext? = null

    fun invoke(callingContext: TraceableContext, value: T):R{
        val warningMsg = "Non suspending invoke call in SuspendingLambda wrapping class"
        warn(subjectInvoke, warningMsg)
        return receiversContext?.let {context->
            runBlocking {
                withContext(context){
                    invoke(value, Unit)
                }
            }
        }?:run {
            val errorMsg = "Impossible to process invoke call in ${componentID.componentName} $warningMsg with no CoroutineContext provided"
            val exception  = IllegalArgumentException(errorMsg)
            callingContext.managedException(exception, immediateOutput = true)
            throw exception
        }
    }

    override suspend fun invoke(value: T, param: Unit) : R = lambda.invoke(value)
}

class SuspendingLambdaWithReceiver<H: TraceableContext, T, R>(
    val receiver : H,
    override val options: SuspendedOptions = SuspendedOptions.Listen,
    private val lambda: suspend H.(T) -> R
):SuspendedWrapper<H, T, R>{

    override val isSuspended: Boolean = true
    override val lambdaName: String = options.name?: lambda::class.lambdaName
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    var receiversContext: CoroutineContext? = null

    constructor(
        context: CoroutineContext,
        receiver : H,
        options: SuspendedOptions = SuspendedOptions.Listen,
        lambda: suspend H.(T) -> R
    ):this(receiver, options, lambda){
        receiversContext = context
    }

    override suspend fun invoke(value: H, param: T): R =  lambda.invoke(value, param)
}