package po.misc.dsl.configurator

import po.misc.callbacks.FunctionalHelper
import po.misc.callbacks.common.EventHost
import po.misc.collections.lambda_list.LambdaWrapper
import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import po.misc.dsl.configurator.data.ConfigurationTracker
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
sealed interface DSLConfigurable<T: TraceableContext, P>: Tokenized<P>, EventHost, FunctionalHelper {
    val groupName: String
    val priority: HasNameValue
    val parameterType: TypeToken<P>
    val configurators: List<LambdaWrapper<T, P>>

    /**
     * Applies all configurators sequentially to the receiver.
     *
     * Implementations must emit lifecycle events and track progress.
     */
    fun applyConfig(configurator: DSLConfigurator<T>, receiver: T, parameter: P): T

    /** Registers a callback invoked when configuration starts. */
    //fun onStart(callback: (ConfigurationTracker<T>) -> Unit)
    /** Registers a callback invoked when configuration completes. */
   // fun onComplete(callback: (ConfigurationTracker<T>) -> Unit)
    /**
     * Dispatches lifecycle events based on current configuration state.
     * @see po.misc.dsl.configurator.data.State
     */
    //fun trigger(tracker: ConfigurationTracker<T>, state: State)
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
sealed class DSLGroupBase<T: TraceableContext, P>(
    override val priority: HasNameValue
) : DSLConfigurable<T, P>{
    override val groupName: String get() =  priority.name
    //internal val onStartSignal: Signal<ConfigurationTracker<T>, Unit> = signalOf()
    //internal val onCompleteSignal: Signal<ConfigurationTracker<T>, Unit> = signalOf()

    protected fun onConfiguration(configurator: DSLConfigurator<T>,  receiver:T,  wrapper: LambdaWrapper<T, P>){
        val tracker = ConfigurationTracker(configurator, receiver, this, wrapper)
        configurator.configurationStep.trigger(tracker)
    }
    protected fun afterConfiguration(configurator: DSLConfigurator<T>, receiver:T,  wrapper: LambdaWrapper<T, P>){
        val tracker = ConfigurationTracker(configurator, receiver, this, wrapper)
        configurator.configurationStep.trigger(tracker)
    }
//    override fun trigger(tracker: ConfigurationTracker<T>, state: State){
//        when(state){
//            State.Initialized-> onStartSignal.trigger(tracker)
//            State.Configurator -> onStartSignal.trigger(tracker)
//            State.Complete -> onCompleteSignal.trigger(tracker)
//            State.Failed -> onCompleteSignal.trigger(tracker)
//        }
//    }

    //override fun onStart(callback: (ConfigurationTracker<T>)-> Unit): Unit = onStartSignal.onSignal(callback)
    //override fun onComplete(callback: (ConfigurationTracker<T>)-> Unit): Unit =  onCompleteSignal.onSignal(callback)
}

class DSLGroup<T: TraceableContext>(
    priority: HasNameValue,
):DSLGroupBase<T, Unit>(priority),  DSLConfigurable<T, Unit>{

    override val parameterType: TypeToken<Unit> = TypeToken.create()
    override val typeToken: TypeToken<Unit> get() = parameterType
    override val configurators : MutableList<LambdaWrapper<T, Unit>> = mutableListOf()

    override fun applyConfig(configurator: DSLConfigurator<T>, receiver: T, parameter: Unit):T{
        configurators.forEach {wrapper->

            val tracker = ConfigurationTracker(configurator, receiver, this, wrapper)
            configurator.configurationStep.trigger(tracker)
            wrapper.apply(receiver, Unit)
            tracker.finalizeStep()
            configurator.configurationStep.trigger(tracker)
        }
        return receiver
    }


    fun addConfigurator(optionalName: String? = null,  block: T.(Unit)-> Unit):DSLGroup<T>{
        val configurator = block.toConfigurator<T>(optionalName)
        configurators.add(configurator)
        return this
    }
    fun applyConfig(configurator: DSLConfigurator<T>, receiver: T): T = applyConfig(configurator, receiver, Unit)
}

class DSLParameterGroup<T: TraceableContext, P>(
    priority: HasNameValue,
    override val parameterType: TypeToken<P>
): DSLGroupBase<T, P>(priority), DSLConfigurable<T, P>{

    override val typeToken: TypeToken<P> get() = parameterType
    override val configurators : MutableList<LambdaWrapper<T, P>> = mutableListOf()

    override fun applyConfig(configurator: DSLConfigurator<T>, receiver: T, parameter:P):T{
        configurators.forEach {wrapper->
            onConfiguration(configurator, receiver,  wrapper)
            wrapper.apply(receiver, parameter)
            afterConfiguration(configurator, receiver, wrapper)
        }
        return receiver
    }
    fun addConfigurator(optionalName: String? = null,  block: T.(P)-> Unit):DSLParameterGroup<T, P>{
        val configurator = block.toConfigurator<T, P>(typeToken,  optionalName)
        configurators.add(configurator)
        return this
    }
    companion object
}