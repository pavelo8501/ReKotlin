package po.misc.functions.containers

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.interfaces.CTX
import po.misc.types.getOrManaged

sealed class ReceivableFunctionContainer<T: Any, P, R: Any?, V: Any>(
    val initialLambda: (T.(P)-> R)? = null
): CTX {

    protected val exceptionPayload: ManagedCallSitePayload = ManagedCallSitePayload(ctx = this)
    abstract val parameter: P


    protected var receiverBacking: T? = null
    protected val receiver:T  get() {
        return receiverBacking.getOrManaged(exceptionPayload.method("receiver get()", "receiverBacking:T"))
    }

    open fun provideReceiver(value:T):ReceivableFunctionContainer<T, P, R, V>{
        receiverBacking = value
        return this
    }

}


class LazyContainerWithReceiver<T: Any, P, R:Any>(
    private val holder: CTX,
    initialLambda: (T.(P)-> R)? = null
): ReceivableFunctionContainer<T, P, R, R>(initialLambda) {

    override val contextName: String
        get() = "LazyContainerWithReceiver On ${holder.contextName}"



    private  var parameterBacking: P? = null
    override val parameter: P get() =  parameterBacking.getOrManaged(exceptionPayload.method("parameter get()", "parameter<P>"))

    override fun provideReceiver(value:T): LazyContainerWithReceiver<T, P, R>{
        receiverBacking = value
        return this
    }

    fun provideReceiverWithParameter(receiver:T, parameter:P): LazyContainerWithReceiver<T, P, R>{
        provideReceiver(receiver)
        parameterBacking = parameter
        return this
    }

    fun provideParameter(parameter:P): LazyContainerWithReceiver<T, P, R>{
        parameterBacking = parameter
        return this
    }

    companion object {

        fun <T: Any, P, R: Any> createWithParameters(
            holder: CTX,
            receiver: T,
            parameter:P
        ):LazyContainerWithReceiver<T, P,  R>{
            val container =  LazyContainerWithReceiver<T, P,  R>(holder)
            return  container.provideReceiverWithParameter(receiver, parameter)
        }

        fun <T: Any, P, R: Any> createWithParameters(
            holder: CTX,
            receiver: T,
            parameter:P,
            lambda: (T.(P)-> R)
        ):LazyContainerWithReceiver<T, P,  R>{
            val container =  LazyContainerWithReceiver<T, P,  R>(holder, lambda)
            return  container.provideReceiverWithParameter(receiver, parameter)
        }
    }
}