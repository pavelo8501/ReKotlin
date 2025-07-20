package po.misc.functions.containers

import po.misc.exceptions.ManagedException
import po.misc.functions.hooks.Change
import po.misc.functions.hooks.DataNotifier
import po.misc.functions.models.ContainerMode
import po.misc.functions.models.LambdaState
import po.misc.functions.models.Updated
import po.misc.types.getOrManaged



data class ContainerResult<T: Any>(
    override val oldValue: T?,
    override val  newValue : T
): Change<T?, T>



/**
 * Abstract base class for [LambdaUnit] implementations that store and reuse input [valueBacking].
 *
 * @param V The input value type.
 * @param R The output result type.
 */
abstract class ResponsiveContainer<V: Any, R: Any>(

):LambdaUnit<V, R>{

    abstract override val function: (V) -> R
    abstract override val identifiedAs: String
    //override val identity : CTXIdentity<ResponsiveContainer<V, R>> = asContext()

    private var stateBacking: LambdaState = LambdaState.Idle
    override val state: LambdaState get() = stateBacking

    override var containerMode: ContainerMode = ContainerMode.Silent

    init {
        message("initialized")
    }

    var resultBacking : R? = null
        private set
    abstract  val result : R

    val containerResult get() = ContainerResult(resultBacking, value)

    protected open var valueBacking : V? = null

    override val value: V
        get() = valueBacking.getOrManaged("value")

    abstract override fun trigger():R

    final override  fun provideValue(value: V){
        valueBacking = value
    }

    final override fun trigger(value: V): R {
        provideValue(value)
        changeState(LambdaState.Waiting)
        trigger()
        return result
    }

     protected fun supplyResultBacking(result:R):R{
        resultBacking = result
        changeState(LambdaState.Complete)
        return result
    }

    protected fun changeState(newState: LambdaState){
        val oldState = state
        stateBacking = newState
        message("Status changed from ${oldState.name} to ${newState.name}")
    }

    protected fun message(msg: String, nonBlockable: Boolean = false){
        if((containerMode == ContainerMode.Verbose) || nonBlockable){
            println(identifiedAs)
            println(msg)
        }
    }
}

/**
 * A simple implementation of [ResponsiveContainer] that executes a value-based lambda
 * without returning any result.
 *
 * Useful for fire-and-forget operations in lifecycle or hook systems.
 *
 * @param V The type of value consumed by the lambda.
 * @param lambda The operation to execute when triggered.
 */
open class Producer<V: Any>(
    override val function: (V) -> Unit
    // override val function: Function<V, Unit>:(V)-> Unit
):ResponsiveContainer<V, Unit>(){

   override val identifiedAs: String get() = "Producer<V>"

    /**
     * The result of triggering this lambda is always [Unit].
     */
    override val result: Unit  get() = Unit

    /**
     * Executes the lambda using the previously provided value.
     * Logs a warning if the value hasn't been set.
     */
    override fun trigger(): Unit {
      val result = valueBacking?.let {
          function.invoke(it)
        }?:println("Value parameter not provided")
        supplyResultBacking(result)
    }

}

/**
 * A simple implementation of [ResponsiveContainer] that produces a value without requiring input.
 *
 * This class encapsulates a parameterless lambda that returns a result of type [R].
 * It can be used in reactive or deferred execution flows where a value must be supplied lazily.
 *
 * @param R The result type of the lambda.
 * @property lambda A function that returns a value of type [R].
 */
open class Provider<R: Any>(
    override val function: (Unit) -> R
): ResponsiveContainer<Unit, R>() {

    override val identifiedAs: String get() = "Provider<R>"

    override val result: R  get() {
       return resultBacking?:run {
            message(
                msg =  "Result requested before explicit trigger, but backing value is null. Forced to trigger early",
                nonBlockable =  true
            )
            trigger()
        }
    }

    /**
     * Executes the lambda and stores its result.
     * @return The result produced by the lambda.
     */
    override fun trigger(): R {
        val result  = function.invoke(Unit)
        return supplyResultBacking(result)
    }
}


/**
 * A simple implementation of [ResponsiveContainer] that evaluates a boolean condition using an input value.
 *
 * This class encapsulates a predicate lambda `(V) -> Boolean`, allowing logic such as validation
 * or filtering to be deferred and executed conditionally.
 *
 * @param V The input value type.
 * @property lambda A predicate function that returns true or false based on the input.
 */
class Evaluator<V: Any>(
    override val function: (V) -> Boolean
): ResponsiveContainer<V, Boolean>()  {

    override val identifiedAs: String get() = "Evaluator<V>"

    override val result: Boolean  get() = resultBacking?:false

    /**
     * Executes the lambda with the current value if available.
     * @return The result of the evaluation, or `false` if no value was provided.
     */
    override fun trigger(): Boolean {
        return valueBacking?.let {
            val result  = function.invoke(it)
            supplyResultBacking(result)
        }?:run {
            supplyResultBacking(false)
            false
        }
    }
}

/**
 * A simple implementation of [ResponsiveContainer] that transforms an input of type [V] into a result of type [R].
 *
 * This class allows for dynamic value adaptation where an input is mapped to a different output.
 * Useful for data transformation in pluggable systems or deferred computation chains.
 *
 * @param V The input value type.
 * @param R The result type.
 * @property lambda A transformation function that maps [V] to [R].
 */
class Adapter<V: Any, R: Any>(
    override val function: (V) -> R
): ResponsiveContainer<V, R>() {

    override val identifiedAs: String get() = "Adapter<V, R>"
    private var ownResult: R? = null

    override val result: R
        get() = resultBacking?:ownResult?: throw ManagedException("Result not available")

    /**
     * Executes the transformation lambda using the current input value if available.
     * @return The transformed result, or null if no input was provided.
     */
    override fun trigger(): R {
        return valueBacking?.let {
            val result  = function.invoke(it)
            supplyResultBacking(result)
        }?:run {
            message("Value parameter not provided")
            throw ManagedException("Value parameter not provided")
        }
    }
}

