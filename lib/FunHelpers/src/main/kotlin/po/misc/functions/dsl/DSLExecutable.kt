package po.misc.functions.dsl

import po.misc.functions.containers.DSLProvider


/**
 * Defines a contract for components that participate in DSL block execution and composition.
 *
 * This interface represents a composable DSL structure that can accumulate processing blocks
 * (`next`) and support nested DSL sub-containers (`with`). It is intended to abstract reusable
 * execution logic in a tree-like DSL builder system.
 *
 * Typical usage includes collecting logic steps (`DSLProvider`s), organizing them into containers,
 * and executing them against a source input type [T], yielding results of type [R].
 *
 * @param T The input type that DSL blocks operate on.
 * @param R The result type produced by each DSL block.
 */
interface DSLExecutable<T: Any, R : Any> {
    val dslBlocks: List<DSLProvider<T, R>>
    val subContainersCount: Int
    val dslBlocksTotalSize: Int

    /**
     * Finalizes this DSL container and returns the resulting structure, potentially
     * resolving deferred blocks or propagating build steps.
     *
     * This is typically called after construction is finished, prior to execution.
     */
    fun build():DSLExecutable<T, R>

    /**
     * Registers a DSL logic block that will be invoked with an instance of [T]
     * and is expected to produce a result of type [R].
     * @param block The DSL operation to register.
     */
    fun next(block: T.() -> R)

    /**
     * Creates and registers a nested sub-container that operates on a derived type [T2].
     * The [valueProvider] extracts a new context value from the current input [T], and
     * [subConstructLambda] defines DSL blocks that will operate on [T2].
     *
     * Useful for splitting logic when [T] contains composable sub-values or delegating
     * processing to other parts of the model.
     *
     * @param valueProvider A transformer from current input [T] to sub-value [T2].
     * @param subConstructLambda DSL construction lambda for the sub-container.
     * @return A new DSL container scoped to [T2].
     */
    fun <T2 : Any> with(valueProvider: (T)-> T2, subConstructLambda: DSLContainer<T2, R>.() -> Unit)
}

sealed interface DSLExecutableBlock<T: Any, R: Any>{

}

sealed interface DSLExecutableSubBlock<T2: Any, T: Any, R: Any>{

}


