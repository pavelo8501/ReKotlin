package po.misc.dsl.configurator

import po.misc.callbacks.FunctionalHelper
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.listen
import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.lambda_list.LambdaWrapper
import po.misc.data.HasNameValue
import po.misc.data.HasValue
import po.misc.functions.NoResult
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken


internal object Unprioritized: HasNameValue{
    override val name: String = "Unprioritized"
    override val value: Int = 0
}

/**
 * Represents a DSL-defined configuration group capable of applying
 * parameterized configuration lambdas (`LambdaWrapper`) to a receiver.
 *
 * Each implementation defines:
 * - configuration priority
 * - parameter type
 * - a sequence of configurators
 * - lifecycle hooks (`onStart`, `onComplete`)
 *
 * @param T type of the receiver
 * @param P type of the configuration parameter
 */
sealed interface DSLConfigurable<T: Any, P>: Tokenized<P>, EventHost, FunctionalHelper {
    val priority: HasNameValue
    val parameterType: TypeToken<P>
    val configurators: List<LambdaWrapper<T, P>>

    /**
     * Applies all configurators sequentially to the receiver.
     *
     * Implementations must emit lifecycle events and track progress.
     */
    fun applyConfig(receiver: T, parameter: P): T
    /** Registers a callback invoked when configuration starts. */
    fun onStart(callback: (ProgressTracker<T>) -> Unit)
    /** Registers a callback invoked when configuration completes. */
    fun onComplete(callback: (ProgressTracker<T>) -> Unit)
    /**
     * Dispatches lifecycle events based on current configuration state.
     * @see State
     */
    fun trigger(tracker: ProgressTracker<T>, state: State)
}

/**
 * Base class for DSL configuration groups.
 *
 * A configuration group defines:
 * - lifecycle signals (`onStart`, `onComplete`)
 * - dispatching of lifecycle events during configuration
 *
 * Groups emit lifecycle callbacks for each configuration run, represented
 * by a `ProgressTracker`. The group itself does not apply configurators but
 * provides helper hooks (`onConfiguration`, `afterConfiguration`) for derived
 * DSL implementations.
 *
 * @param T type of the receiver being configured
 * @param P type of parameter passed to configuration lambdas
 */
sealed class DSLGroupBase<T: Any, P>() : DSLConfigurable<T, P>{

    internal val onStartSignal: Signal<ProgressTracker<T>, Unit> = signalOf()
    internal val onCompleteSignal: Signal<ProgressTracker<T>, Unit> = signalOf()

    protected fun onConfiguration(receiver:T,  wrapper: LambdaWrapper<T, P>){
        val tracker = ProgressTracker(receiver, this, wrapper)
        onStartSignal.trigger(tracker)
    }
    protected fun afterConfiguration(receiver:T,  wrapper: LambdaWrapper<T, P>){
        val tracker = ProgressTracker(receiver, this, wrapper)
        onStartSignal.trigger(tracker)
    }
    override fun trigger(tracker: ProgressTracker<T>, state: State){
        when(state){
            State.Initialized-> onStartSignal.trigger(tracker)
            State.Configurator -> onStartSignal.trigger(tracker)
            State.Complete -> onCompleteSignal.trigger(tracker)
            State.Failed -> onCompleteSignal.trigger(tracker)
        }
    }
    override fun onStart(callback: (ProgressTracker<T>)-> Unit): Unit = onStartSignal.onSignal(callback)
    override fun onComplete(callback: (ProgressTracker<T>)-> Unit): Unit =  onCompleteSignal.onSignal(callback)
}

class DSLGroup<T: Any>(
    override val priority: HasNameValue,
):DSLGroupBase<T, Unit>(),  DSLConfigurable<T, Unit>{

    override val parameterType: TypeToken<Unit> = TypeToken.create()
    override val typeToken: TypeToken<Unit> get() = parameterType
    val groupName: String = priority.name
    override val configurators : MutableList<LambdaWrapper<T, Unit>> = mutableListOf()

    override fun applyConfig(receiver: T, parameter: Unit):T{
        configurators.forEach {wrapper->
            onConfiguration(receiver, wrapper)
            wrapper.apply(receiver, Unit)
            afterConfiguration(receiver, wrapper)
        }
        return receiver
    }
    fun addConfigurator(optionalName: String? = null,  block: T.(Unit)-> Unit):DSLGroup<T>{
        val configurator = block.toConfigurator<T>(optionalName)
        configurators.add(configurator)
        return this
    }
    fun applyConfig(receiver: T): T = applyConfig(receiver, Unit)

}

class DSLParameterGroup<T: Any, P>(
    override val priority: HasNameValue,
    override val parameterType: TypeToken<P>
): DSLGroupBase<T, P>(), DSLConfigurable<T, P>{

    override val typeToken: TypeToken<P> get() = parameterType
    val groupName: String = priority.name
    override val configurators : MutableList<LambdaWrapper<T, P>> = mutableListOf()

    override fun applyConfig(receiver: T, parameter:P):T{
        configurators.forEach {wrapper->
            onConfiguration(receiver,  wrapper)
            wrapper.apply(receiver, parameter)
            afterConfiguration(receiver, wrapper)
        }
        return receiver
    }
    fun addConfigurator(optionalName: String? = null,  block: T.(P)-> Unit):DSLParameterGroup<T, P>{
        val configurator = block.toConfigurator<T, P>(typeToken,  optionalName)
        configurators.add(configurator)
        return this
    }

    companion object{

    }
}