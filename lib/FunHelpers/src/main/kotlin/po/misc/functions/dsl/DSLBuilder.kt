package po.misc.functions.dsl



interface DSLBuilder<T : Any, R: Any> {
    val dslContainer: DSLContainer<T, R>
}