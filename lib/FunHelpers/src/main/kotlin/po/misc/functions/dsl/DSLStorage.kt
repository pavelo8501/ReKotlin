package po.misc.functions.dsl


interface DSLStorage<T: Any, R : Any> {
    val dslBlocks: List<DSLProvider<T, R>>
    val subContainersCount: Int
    val dslBlocksTotalSize: Int
    fun build():DSLStorage<T, R>
    fun next(block: T.() -> R)
    fun <T2 : Any> with(valueProvider: (T)-> T2, subConstructLambda: DSLContainer<T2, R>.() -> Unit): DSLContainer<T2, R>
}

