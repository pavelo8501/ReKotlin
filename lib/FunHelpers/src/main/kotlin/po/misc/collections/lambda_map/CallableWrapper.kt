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
import po.misc.types.k_function.lambdaName
import po.misc.types.k_function.receiverClass
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KFunction

interface CallableWrapper<T, R>: Component  {
    val lambdaName: String
    val options: CallableOptions
    val isSuspended: Boolean
    fun invoke(value: T):R
    suspend fun invokeSuspending(value: T):R
}

class Lambda<T, R>(
    override val options: LambdaOptions = LambdaOptions.Listen,
    private val lambda:(T) -> R
):CallableWrapper<T, R>{

    override val isSuspended: Boolean = false
    override val lambdaName: String = options.name?: lambda::class.lambdaName

    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    constructor(lambda: (T) -> R): this(LambdaOptions.Listen, lambda)

    override fun invoke(value: T) : R = lambda.invoke(value)
    override suspend fun invokeSuspending(value: T) : R = invoke(value)
}

class LambdaWithReceiver<H: TraceableContext, T, R>(
    val receiver : H,
    override val options: LambdaOptions = LambdaOptions.Listen,
    private val lambda: H.(T) -> R
):CallableWrapper<T, R> {

    override val isSuspended: Boolean = false
    override val lambdaName: String = options.name?: lambda::class.lambdaName

    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    constructor(receiver: H, lambda: H.(T) -> R) : this(receiver, LambdaOptions.Listen, lambda)

    override fun invoke(value: T): R = lambda.invoke(receiver, value)
    override suspend fun invokeSuspending(value: T) : R = invoke(value)
}

class SuspendingLambda<T, R>(
    override val options: SuspendedOptions = SuspendedOptions.Listen,
    private val lambda: suspend (T) -> R
):CallableWrapper<T, R>{

    override val isSuspended: Boolean = true
    override val lambdaName: String = options.name?: lambda::class.lambdaName
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    private val subjectInvoke = "Invoke call"

    var receiversContext: CoroutineContext? = null

    constructor(
        context: CoroutineContext,
        options: SuspendedOptions = SuspendedOptions.Listen,
        lambda: suspend (T) -> R
    ): this(options, lambda){
        receiversContext = context
    }

    fun invoke(callingContext: TraceableContext, value: T):R{
        val warningMsg = "Non suspending invoke call in SuspendingLambda wrapping class"
        warn(subjectInvoke, warningMsg)
        return receiversContext?.let {context->
            runBlocking {
                withContext(context){
                    invokeSuspending(value)
                }
            }
        }?:run {
            val errorMsg = "Impossible to process invoke call in ${componentID.componentName} $warningMsg with no CoroutineContext provided"
            val exception  = IllegalArgumentException(errorMsg)
            callingContext.managedException(exception, immediateOutput = true)
            throw exception
        }
    }

    override suspend fun invokeSuspending(value: T) : R = lambda.invoke(value)
    override fun invoke(value: T) : R = invoke(this, value)
}


class SuspendingLambdaWithReceiver<H: TraceableContext, T, R>(
    val receiver : H,
    override val options: SuspendedOptions = SuspendedOptions.Listen,
    private val lambda: suspend H.(T) -> R
):CallableWrapper<T, R>{

    override val isSuspended: Boolean = true
    override val lambdaName: String = options.name?: lambda::class.lambdaName
    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    private val subjectInvoke = "Invoke call"
    var receiversContext: CoroutineContext? = null

    constructor(
        receiver : H,
        lambda: suspend H.(T) -> R
    ):this(receiver, SuspendedOptions.Listen, lambda)

    constructor(
        context: CoroutineContext,
        receiver : H,
        options: SuspendedOptions = SuspendedOptions.Listen,
        lambda: suspend H.(T) -> R
    ):this(receiver, options, lambda){
        receiversContext = context
    }

    fun invoke(callingContext: TraceableContext, value: T) : R {
        val warningMsg = "Non suspending invoke call in SuspendingLambda wrapping class"
        warn(subjectInvoke, warningMsg)
        return receiversContext?.let { context ->
            runBlocking {
                withContext(context) {
                    invokeSuspending(value)
                }
            }
        }?: run {
            val errorMsg = "Impossible to process invoke call in ${componentID.componentName} $warningMsg with no CoroutineContext provided"
            val exception  = IllegalArgumentException(errorMsg)
            callingContext.managedException(exception, immediateOutput = true)
            throw exception
        }
    }

    override suspend fun invokeSuspending(value: T) : R =  lambda.invoke(receiver, value)
    override fun invoke(value: T) : R = invoke(this, value)
}