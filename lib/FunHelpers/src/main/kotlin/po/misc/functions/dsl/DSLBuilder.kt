package po.misc.functions.dsl


/**
 * Marker interface for components capable of owning a [DSLContainer].
 *
 * This interface indicates that an implementing class provides access
 * to a [dslContainer], which allows building and resolving DSL-based
 * logic over inputs of type [T] producing results of type [R].
 *
 * @param T The input data type the DSL operates on.
 * @param R The result type produced by DSL blocks.
 */
interface DSLBuilder<T : Any, R: Any> {
    val dslContainer: DSLContainer<T, R>
}


/**
 * Marker interface for components capable of owning a [DSLConstructor].
 *
 * This interface indicates that an implementing class provides access
 * to a [dslConstructor], which allows building and resolving DSL-based
 * logic over inputs of type [T] producing results of type [R].
 *
 * @param T The input data type the DSL operates on.
 * @param R The result type produced by DSL blocks.
 */
interface ConstructableDSL<T : Any, R: Any> {

}
