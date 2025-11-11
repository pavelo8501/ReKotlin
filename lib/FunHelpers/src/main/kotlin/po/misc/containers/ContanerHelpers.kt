package po.misc.containers


/**
 * Executes the given [block] with [receiver] as the lambda receiver.
 * Enables DSL-like scoping to expose the [receiver]'s functions and properties directly.
 * Useful when working with nested containers or delegating context behavior.
 *
 * Example use case:
 * ```kotlin
 * someContainer.withReceiver {
 *     // 'this' refers to the receiver, not the container
 *     hello()
 * }
 * ```
 * @receiver A [ReceiverContainer] whose [receiver] will be passed as lambda receiver.
 * @param block The lambda to execute in the [receiver]'s scope.
 */
inline fun <C:ReceiverContainer<T>, T: Any> C.withReceiver(block: T.() -> Unit){
    block.invoke(receiver)
}


/**
 * Executes the given [block] with [receiver] as the lambda receiver and returns its result.
 *
 * Similar to [withReceiver], but allows returning a value from the scoped block.
 *
 * @receiver A [ReceiverContainer] whose [receiver] will be passed as lambda receiver.
 * @param block The lambda to execute with the [receiver] as receiver.
 * @return The result of the executed [block].
 */
inline fun <C:ReceiverContainer<T>,  T: Any, R> C.withReceiverAndResult(block: T.() -> R):R{
   return block.invoke(receiver)
}

suspend fun <C:ReceiverContainer<T>,  T: Any, R> C.withReceiverSuspended(block: suspend T.() -> R):R{
    return block.invoke(receiver)
}