package po.misc.functions.interfaces


/**
 * Interface representing a functional computation container that optionally holds
 * a resolved value and information about its availability.
 *
 * @param T The value type being managed or produced.
 */
interface FunctionalClass<T: Any?>{

    /**
     * Indicates whether a lambda (computation provider) has been assigned.
     */
    val isLambdaProvided: Boolean

    /**
     * Indicates whether the value has been successfully resolved at least once.
     */
    val isResolved: Boolean

    /**
     * The latest resolved value, if available.
     */
    val currentValue: T?
}