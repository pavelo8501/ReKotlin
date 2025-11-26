package po.misc.functions.dsl

/**
 * Marker interface for components capable of owning a [DSLContainer].
 *
 * Implementing this interface allows a class to participate in DSL construction
 * by exposing a [dslContainer]. It also enables the use of the [dslBuilder] extension
 * function, which simplifies the process of building and resolving nested DSL blocks.
 *
 * This is typically used as a hook to embed DSL logic declaratively inside a
 * larger structure or coordinator.
 *
 * @param T The input data type the DSL operates on.
 * @param R The result type produced by DSL blocks.
 *
 * @see dslBuilder
 */
fun <T : Any, R : Any>  DSLBuilder<T,R>.dslBuilder(block: DSLContainer<T, R>.() -> Unit): DSLContainer<T, R> {
    dslContainer.block()
    val container = DSLContainer<T, R>(block)
    container.build()
    return container
}


inline fun <T: Any, T2: Any, R> withBlockFlattened(
    crossinline adapter: (T) -> T2,
    crossinline block: T2.() -> R
): T.() -> R = {
    val adapted = adapter(this)
    block(adapted)
}
