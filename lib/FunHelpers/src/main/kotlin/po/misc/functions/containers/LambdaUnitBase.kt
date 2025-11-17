package po.misc.functions.containers

import po.misc.functions.models.ContainerMode
import po.misc.functions.models.LambdaState
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken


sealed interface DuplexUnit<V : Any, R> : LambdaUnit<V, R> {
    val function: Function1<V, R>
    val resultHandler: ResultHandler<V, R>
}

sealed interface ParametrizedUnit<V : Any, P : Any?, R : Any> : LambdaUnit<V, R> {
    val function: Function2<V, P, R>
    val resultHandler: ResultHandler<V, R>
}

sealed interface NoResultLambda<V : Any> : LambdaUnit<V, Unit> {
    val function: Function1<V, Unit>

    override fun trigger(value: V)
}

sealed interface DeferredUnit<R> {
    val function: Function<R>
    val resultHandler: ResultHandler<Unit, R>
}

data class LambdaUnitConfig(
    val cacheResult: Boolean = false,
    val cacheValue: Boolean = false,
    val containerMode: ContainerMode = ContainerMode.Silent,
)

class ResultHandler<V : Any, R> {
    var resultProvided: ((R) -> Unit)? = null

    fun onResultProvided(onResult: (R) -> Unit) {
        resultProvided = onResult
    }

    fun provideResult(
        result: R,
        provider: LambdaUnit<V, R>,
    ) {
        when (provider) {
            is DeferredUnit<*> -> {
                resultProvided?.invoke(result)
            }
            is DuplexUnit -> {
                resultProvided?.invoke(result)
            }
        }
    }
}

/**
 * Abstract base class for [LambdaUnit] implementations that store and reuse input [valueBacking].
 *
 * @param V The input value type.
 * @param R The output result type.
 */
abstract class LambdaUnitBase<V : Any, R>(
    val config: LambdaUnitConfig? = null,
) : LambdaUnit<V, R>{
    abstract override val identifiedAs: String

    private var stateBacking: LambdaState = LambdaState.Idle
    override val state: LambdaState get() = stateBacking

    private val effectiveConfig: LambdaUnitConfig = config ?: LambdaUnitConfig()
    override val containerMode: ContainerMode get() = effectiveConfig.containerMode

    protected open var valueBacking: V? = null
    override val persistedValue: V get() = valueBacking.getOrManaged(this, Any::class)

    private var resultBacking: R? = null
    val result: R get() {
        return try {
            resultBacking!!
        } catch (th: Throwable) {
            fallbackOrThrow(th)
        }
    }

    init {
        message("initialized")
        changeState(LambdaState.Idle)
    }

    protected fun onTriggered(instance: NoResultLambda<*>) {
    }

    private var exceptionFallback: ((Throwable) -> R)? = null

    fun exceptionFallback(fallback: (Throwable) -> R) {
        exceptionFallback = fallback
    }

    private fun fallbackOrThrow(exception: Throwable): R =
        exceptionFallback?.invoke(exception) ?: run {
            message("Throwing exception since no fallback provided")
            throw exception
        }

    final override fun provideValue(value: V) {
        valueBacking = value
        changeState(LambdaState.Waiting)
    }

    protected fun provideResult(result: R) {
        resultBacking = result
    }

    private fun clearResult() {
        resultBacking = null
    }

    private fun clearValue() {
        valueBacking = null
    }

    protected fun clearCached() {
        clearResult()
        clearValue()
    }

    abstract override fun trigger(value: V): R

    protected fun changeState(newState: LambdaState) {
        val oldState = state
        stateBacking = newState
        message("Status changed from ${oldState.name} to ${newState.name}")
    }

    protected fun message(
        msg: String,
        nonBlockable: Boolean = false,
    ) {
        if ((containerMode == ContainerMode.Verbose) || nonBlockable) {
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
open class Notifier<V : Any>(
    override var function: (V) -> Unit,
) : LambdaUnitBase<V, Unit>(),
    NoResultLambda<V> {
    override val identifiedAs: String get() = "Producer<V>"

    override fun trigger(value: V) {
        function.invoke(value)
        onTriggered(this)
        changeState(LambdaState.Complete)
    }

    fun triggerUnsubscribing(value: V) {
        function.invoke(value)
        onTriggered(this)
        changeState(LambdaState.Complete)
        unsubscribe()
    }

    fun unsubscribe() {
        clearCached()
        function = {}
    }
}

fun <V : Any> lambdaAsNotifier(function: (V) -> Unit): Notifier<V> = Notifier(function)

class DSLNotifier<T : Any, P : Any>(
    val typeData: TypeToken<T>,
    val parameter: P,
    override val function: T.(P) -> Unit,
) : LambdaUnitBase<T, Unit>(),
    ParametrizedUnit<T, P, Unit> {
    override val identifiedAs: String get() = "DSLProvider<T, R>"
    override val resultHandler: ResultHandler<T, Unit> = ResultHandler()
    var modifiedResult:T? = null

    override fun trigger(value: T): Unit {
        val result = function.invoke(value, parameter)
        modifiedResult = value
        resultHandler.provideResult(result, this)
        return result
    }

    fun getModified(callingContext: Any):T{
       return modifiedResult.getOrManaged(callingContext, typeData.kClass)
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
class Provider<R : Any>(
    override val function: () -> R,
) : LambdaUnitBase<Unit, R>(),
    DeferredUnit<R> {
    override val identifiedAs: String get() = "Provider<R>"

    override val resultHandler: ResultHandler<Unit, R> = ResultHandler()

    override fun trigger(value: Unit): R {
        val result = function.invoke()
        resultHandler.provideResult(result, this)
        return result
    }

    fun trigger(): R = trigger(Unit)
}

class NullableProvider<R : Any>(
    private val initialLambda: (() -> R)? = null,
) : LambdaUnitBase<Unit, R>(),
    DeferredUnit<R> {
    var isUserDefinedFunction: Boolean = false
        private set

    override var function: () -> R = {
        result
    }
    override val identifiedAs: String get() = "NullableProvider<R>"
    override val resultHandler: ResultHandler<Unit, R> = ResultHandler()

    init {
        initialLambda?.let {
            isUserDefinedFunction = true
            function = it
        }
    }

    fun subscribe(lambda: (() -> R)) {
        isUserDefinedFunction = true
        function = lambda
    }

    override fun trigger(value: Unit): R {
        val result = function.invoke()
        resultHandler.provideResult(result, this)
        provideResult(result)
        return result
    }

    fun trigger(): R = trigger(Unit)

    fun dispose() {
        function = {
            result
        }
    }
}

class DSLProvider<T : Any, R : Any>(
    override val function: T.() -> R,
) : LambdaUnitBase<T, R>(),
    DuplexUnit<T, R> {
    override val identifiedAs: String get() = "DSLProvider<T, R>"
    override val resultHandler: ResultHandler<T, R> = ResultHandler()

    override fun trigger(value: T): R {
        val result = function.invoke(value)
        resultHandler.provideResult(result, this)
        return result
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
class Evaluator<V : Any>(
    override val function: (V) -> Boolean,
) : LambdaUnitBase<V, Boolean>(),
    DuplexUnit<V, Boolean> {
    override val identifiedAs: String get() = "Evaluator<V>"
    override val resultHandler: ResultHandler<V, Boolean> = ResultHandler()

    override fun trigger(value: V): Boolean {
        val result = function.invoke(value)
        changeState(LambdaState.Complete)
        resultHandler.provideResult(result, this)
        return result
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
class Adapter<V : Any, R : Any>(
    override val function: (V) -> R,
) : LambdaUnitBase<V, R>(),
    DuplexUnit<V, R> {
    override val identifiedAs: String get() = "Adapter<V, R>"
    override val resultHandler: ResultHandler<V, R> = ResultHandler()

    override fun trigger(value: V): R {
        val result = function.invoke(value)
        changeState(LambdaState.Complete)
        resultHandler.provideResult(result, this)
        return result
    }
}

class DSLAdapter<T : Any, P : Any, R : Any>(
    val parameter: P,
    override val function: T.(P) -> R,
) : LambdaUnitBase<T, R>(),
    ParametrizedUnit<T, P, R> {
    override val identifiedAs: String get() = "DSLProvider<T, R>"
    override val resultHandler: ResultHandler<T, R> = ResultHandler()

    override fun trigger(value: T): R {
        val result = function.invoke(value, parameter)
        resultHandler.provideResult(result, this)
        return result
    }
}


