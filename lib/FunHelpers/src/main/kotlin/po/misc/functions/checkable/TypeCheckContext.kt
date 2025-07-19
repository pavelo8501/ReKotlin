package po.misc.functions.checkable


/**
 * Represents a container for runtime type-checking and conditional execution logic.
 * Provides utilities like [whenOfType] for type-safe branching and [fallback] for
 * reacting when no condition is matched.
 */
sealed interface TypeCheckContext{
    /**
     * The instance being tested against a specific type.
     */
    val testedInstance: Any
    /**
     * Registers a fallback action to be executed if no condition is satisfied.
     * @param onFallback A lambda to invoke if all type checks fail.
     */
    fun fallback(onFallback :()-> Unit)
}


/**
 * Default implementation of [TypeCheckContext] used to wrap a test instance for evaluation.
 * Internally manages the result of type-check evaluations and triggers a fallback
 * if needed.
 * @param testedInstance The value to be type-checked during branching.
 */
class CheckableInstance(override val testedInstance: Any): TypeCheckContext{
    private var onFallbackHook: (() -> Unit)? = null
    private var providedResult: Boolean? = null
    override fun fallback(onFallback: () -> Unit) {
        onFallbackHook = onFallback
        providedResult?.let { result->
            if(!result){
                onFallback.invoke()
            }
        }
    }
    @PublishedApi
    internal fun  provideResult(checkResult: Boolean){
        providedResult = checkResult
        if(!checkResult && onFallbackHook != null){
            onFallbackHook?.invoke()
        }
    }
}
