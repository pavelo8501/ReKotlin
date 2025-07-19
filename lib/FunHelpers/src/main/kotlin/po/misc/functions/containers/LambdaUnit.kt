package po.misc.functions.containers

import po.misc.context.CTX

/**
 * A reactive unit abstraction that supports deferred triggering with a value of type [V]
 * and returns a result of type [R].
 * This is typically used in hook systems to encapsulate reusable, pluggable behavior.
 * @param V The input value type the lambda expects.
 * @param R The result type the lambda produces.
 */
sealed interface LambdaUnit<V: Any, R: Any?>{

    /**
     * Triggers the lambda without passing a value (assumes it was provided earlier).
     */
    fun trigger():R

    /**
     * Triggers the lambda using the given [value].
     */
    fun trigger(value: V):R

    /**
     * Provides a [value] for later use by [trigger] without parameters.
     */
    fun provideValue(value: V)


}





