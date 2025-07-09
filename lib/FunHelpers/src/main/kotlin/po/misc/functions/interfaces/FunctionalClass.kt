package po.misc.functions.interfaces


interface FunctionalClass<T: Any>{
    val isLambdaProvided: Boolean

    /** Optional: whether the value has been resolved at least once */
    val isResolved: Boolean

    /** Optional: access to the latest resolved value */
    val currentValue: T?
}