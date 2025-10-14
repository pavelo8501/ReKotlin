package po.misc.functions.containers

import po.misc.functions.hooks.BasicHooks
import po.misc.functions.hooks.ReactiveHooks
import po.misc.functions.interfaces.FunctionalClass
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.types.getOrManaged


/**
 * Abstract base class for reactive functional containers with lifecycle hook support.
 *
 * @param T The receiver/input type for the lambda.
 * @param R The raw result type produced by the lambda.
 * @param V The externally visible value (e.g., input, output, or transformed result).
 * @param initialLambda Optional lambda provider assigned during construction.
 */
sealed class BasicFunctionContainer<T: Any, R: Any?, V: Any?>(
  val holder: CTX,
  initialLambda: ((T)->R)? = null,
  val hooks: ReactiveHooks<BasicFunctionContainer<T, R, V>, V> = ReactiveHooks()
):  FunctionalClass<V>, CTX, BasicHooks<BasicFunctionContainer<T, R, V>, V> by hooks{

    override val identity : CTXIdentity<BasicFunctionContainer<T, R, V>> = asSubIdentity(holder)

    /**
     * Hook manager for this container.
     */
   // override val hooks: ReactiveHooks<BasicFunctionContainer<T, R, V>, V> = ReactiveHooks(this)

    private var lambdaBacking: ((T)->R)? = null
    protected val lambda: ((T)->R) get(){
        return lambdaBacking.getOrManaged(this)
    }
    override val isLambdaProvided: Boolean get() = lambdaBacking != null

    abstract  val receiver:T

    protected var resultBacking: R? = null
    protected val  result:R  get() {
        return resultBacking.getOrManaged(this)
    }

    private var valueBacking:V? = null
    open val value: V get() = valueBacking.getOrManaged(this)
    /**
     * The latest externally visible value, if available.
     */
    override val currentValue: V? get() = valueBacking
    /**
     * Indicates whether a value has been resolved and is available.
     */
    val isValueAvailable: Boolean get() =  valueBacking != null

    override val isResolved: Boolean get() = isValueAvailable && isLambdaProvided

    init {
        initialLambda?.let {
            registerProvider(it)
        }
    }

    private fun clearInternal(){
        valueBacking = null
        lambdaBacking = null
        //receiverBacking = null
    }

    fun notifyChanged(old: V?, new: V){
        hooks.fireChange(old, new)
    }

    protected fun provideValue(value:V){
        if(valueBacking != value){
            notifyChanged(valueBacking, value)
        }
        valueBacking = value
    }

    /**
     * Registers a new lambda provider and triggers the [onProviderSet] hook.
     */
    fun registerProvider(block: (T) -> R):BasicFunctionContainer<T, R, V> {
        lambdaBacking = block
        hooks.fireProviderSet()
        return this
        //registerParametrizedProvider { _: P -> block() }
    }

    protected abstract fun receiverProvided(value:T)

    /**
     * Allows child classes to provide the input receiver.
     */
   open fun provideReceiver(value:T):BasicFunctionContainer<T, R, V>{
       // receiverBacking = value
        receiverProvided(value)
        return this
    }

    /**
     * Resolves the lambda using the provided receiver, updates result and value, and fires hooks.
     */
    fun resolve(receiver:T):V{
       hooks.fireBeforeResolve()
      // receiverBacking = receiver
       receiverProvided(receiver)
       resultBacking = lambda.invoke(receiver)
       hooks.fireResolved(value)
       return value
    }

    /**
     * Resolves the lambda using the previously provided receiver, updates result and value, and fires hooks.
     */
    open fun resolve():V{
        hooks.fireBeforeResolve()
        resultBacking = lambda.invoke(receiver)
        hooks.fireResolved(value)
        return value
    }

    fun disposeHooks() {
        hooks.disposeHooks()
    }

    /**
     * Disposes this container and clears references. Optionally fires and clears hooks.
     *
     * @param withHooks Whether to fire [onDispose] and clear hook bindings.
     */
    fun dispose(withHooks: Boolean){
        clearInternal()
        if(withHooks){
            disposeHooks()
        }else{
            hooks.fireDispose()
        }
    }
}

/**
 * A reactive container for actions that consume a value of type [T] and return `Unit`.
 *
 * Internally tracks the most recent input value as the externally visible [value],
 * and supports all reactive lifecycle hooks via [ReactiveHooks].
 *
 * @param T The input type accepted by the lambda function.
 * @param holder A parent context used for naming and identification.
 * @param initialLambda An optional lambda to register during initialization.
 */
class LambdaHolder<T: Any>(
    holder: CTX ,
    initialLambda: ((T)-> Unit)? = null
): BasicFunctionContainer<T, Unit, T>(holder , initialLambda) {

    /**
     * A unique name for the container based on the provided context.
     */
    override val contextName: String
        get() = "LambdaContainer On ${holder.contextName}"



    private var valueBacking:T? = null
    override val receiver: T
        get() = valueBacking.getOrManaged(this, Any::class)

    /**
     * Returns the last receiver value that was used during resolution.
     * Throws a detailed exception if the receiver is not yet available.
     */
    override val value: T get(){
        return valueBacking.getOrManaged(this, Any::class)
    }

    override fun receiverProvided(value: T) {
        valueBacking = value
        provideValue(value)
    }

    /**
     * Sets the input receiver and provides it as the current value.
     *
     * @param value The input to be consumed by the lambda.
     */
    override fun provideReceiver(value:T): LambdaHolder<T>{
        receiverProvided(value)
        return this
    }

    /**
     * Resolves the lambda using the stored receiver, updates the visible value,
     * and triggers all resolution-related hooks.
     *
     * @return The input receiver used (same as [value]).
     */
    override fun resolve():T{
        val resolvedValue =  super.resolve()
        provideValue(resolvedValue)
        return value
    }
}

/**
 * A reactive container that resolves a lambda with no input (`Unit`) to produce a result of type [R].
 *
 * Designed for lazy evaluation of deferred computations, it tracks the result as [value] and supports
 * all reactive lifecycle hooks via [ReactiveHooks].
 *
 * @param R The type of the result returned by the lambda.
 * @param holder A parent context used for naming and identification.
 * @param initialLambda An optional lambda to register during initialization.
 */
class DeferredContainer<R>(
    holder: CTX,
    initialLambda: ((Unit)-> R)? = null
): BasicFunctionContainer<Unit, R, R>(holder, initialLambda) where R: Any?{

    /**
     * A unique name for the container based on the provided context.
     */
    override val contextName: String
         get() = "DeferredContainer On ${holder.contextName}"



    override val receiver: Unit = Unit
    override fun receiverProvided(value: Unit) {

    }

    /**
     * Returns the latest resolved result.
     * Throws a detailed exception if the result is not yet available.
     */
    override val value: R get(){
        return resultBacking.getOrManaged(this)
    }

    /**
     * Resolves the lambda with `Unit` as input, caches the result,
     * and triggers all resolution-related hooks.
     *
     * @return The resolved result.
     */
    override fun resolve():R{
        hooks.fireBeforeResolve()
        val resolvedValue =  super.resolve()
        provideValue(resolvedValue)
        return value
    }
}


/**
 * A reactive container for deferred computations that require an input of type [T]
 * and produce a result of type [R].
 *
 * Unlike [DeferredContainer], which operates with `Unit` input, this variant explicitly
 * receives input via [provideReceiver] or [provideValue] before resolution.
 *
 * The latest resolved result is exposed as [value], and all reactive lifecycle hooks
 * from [ReactiveFunctionContainer] are fully supported.
 *
 * @param T The input type for the lambda function.
 * @param R The result type returned by the lambda.
 * @param holder A parent context used for diagnostics and contextual naming.
 * @param initialLambda Optional lambda function to assign on initialization.
 */
class LazyExecutionContainer<T : Any, R : Any>(
    holder: CTX,
    initialLambda: ((T)-> R)? = null
): BasicFunctionContainer<T, R, R>(holder, initialLambda) {

    /**
     * A unique name for the container, based on the parent context.
     */
    override val contextName: String
        get() = "DeferredInputContainer On ${holder.contextName}"

    /**
     * The most recently resolved result.
     * Throws a descriptive exception if the result is unavailable.
     */


    private var receiverBacking:T? = null
    override val receiver: T get() = receiverBacking.getOrManaged(this, Any::class)

    override val value: R get(){
        return resultBacking.getOrManaged(this, Any::class)
    }

    override fun receiverProvided(value: T) {
        receiverBacking = value
    }

    /**
     * Assigns the input value that will be used during resolution.
     *
     * @param value The input value for the lambda.
     */
    override fun provideReceiver(value:T):LazyExecutionContainer<T, R>{
        receiverProvided(value)
        return this
    }

    /**
     * Alias for [provideReceiver], included for symmetry with [DeferredContainer].
     *
     * @param value The input value to provide.
     */
    fun provideInput(value:T){
        provideReceiver(value)
    }

    /**
     * Resolves the lambda using the previously provided input,
     * stores the result as [value], and fires the appropriate hooks.
     *
     * @return The result produced by the lambda.
     */
    override fun resolve():R{
        val resolvedValue =  super.resolve()
        provideValue(resolvedValue)
        return value
    }
}
