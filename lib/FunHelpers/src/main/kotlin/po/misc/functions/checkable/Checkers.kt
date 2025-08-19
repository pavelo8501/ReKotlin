package po.misc.functions.checkable


/**
 * Executes [action] if the [testedInstance] is of type [T].
 *
 * This function supports chaining with [fallback] to handle unmatched types.
 *
 * @param T The type to test against.
 * @param testedInstance Optional override of the instance to test; defaults to [TypeCheckContext.testedInstance].
 * @param action A lambda with [T] as the receiver, executed if type matches.
 * @return The current [TypeCheckContext] for chaining.
 *
 * Example:
 * ```
 * someObject.checkAndRun {
 *     onType<MyType> {
 *         println("It's a MyType!")
 *     }.fallback {
 *         println("Not matched.")
 *     }
 * }
 * ```
 */
inline fun <reified T: Any> TypeCheckContext.onType(testedInstance: Any = this.testedInstance, action:T.()-> Unit):TypeCheckContext{
    if(testedInstance is T){
        action.invoke(testedInstance)
        (this as? CheckableInstance)?.provideResult(true)
    }else{
        (this as? CheckableInstance)?.provideResult(false)
    }
   return this
}


/**
 * Attempts to safely cast [testedInstance] to type [T].
 *
 * @param T The target type to cast to.
 * @param testedInstance Optional override of the instance to cast; defaults to [TypeCheckContext.testedInstance].
 * @return The casted instance if successful, or `null` if the type does not match.
 *
 * Example:
 * ```
 * val obj = checkable.asType<MyType>()
 * obj?.doSomething()
 * ```
 */
inline fun <reified T : Any> TypeCheckContext.asType(testedInstance: Any = this.testedInstance): T? =
    testedInstance as? T


/**
 * Starts a type-checking flow by wrapping the receiver into a [TypeCheckContext] and applying [block].
 *
 * Useful for creating clean, scoped conditional logic based on runtime types.
 *
 * @receiver The instance to check.
 * @param block A lambda with [TypeCheckContext] context to define type-checking branches.
 *
 * Example:
 * ```
 * someInstance.checkAndRun {
 *     whenOfType<MyType> { println("It's MyType") }
 *     fallback { println("Unknown type") }
 * }
 * ```
 */
fun <T: Any> T.checkAndRun(block: TypeCheckContext.()-> Unit){
    block.invoke(CheckableInstance(this))
}