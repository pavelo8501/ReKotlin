package po.misc.functions.dsl

import po.misc.functions.containers.Adapter
import po.misc.functions.containers.DSLProvider

import po.misc.types.safeCast


class DSLContainer<T, R>(
    private var constructLambda : (DSLContainer<T, R>.() -> Unit)? = null
): DSLExecutable<T, R> where T: Any, R:Any {

    private var dataProvider : Adapter<Any, T>? = null

    private val subContainers: MutableList<DSLContainer<*, R>> = mutableListOf()

    private val dslBlocksBacking: MutableList<DSLProvider<T, R>> = mutableListOf()

    override val dslBlocks: List<DSLProvider<T, R>> get() = dslBlocksBacking

    override val subContainersCount: Int get() = subContainers.size
    override val dslBlocksTotalSize: Int get() = subContainers.sumOf { it.dslBlocksBacking.size } + dslBlocksBacking.size

    private fun <T2: Any> createDataAdapter(providerLambda : (T2)->T ){
         providerLambda.safeCast<(Any)->T>()?.let {
             dataProvider = Adapter(it)
         }
    }

    override fun <T2 : Any> with(valueProvider: (T)-> T2, subConstructLambda: DSLContainer<T2, R>.() -> Unit) {
        val container =  DSLContainer(subConstructLambda)
        container.createDataAdapter(valueProvider)
        subContainers.add(container)
        container.build()
        //return container
    }

    override fun next(block: T.() -> R) {
        val newProvider = DSLProvider(block)
        dslBlocksBacking.add(newProvider)
    }
    override fun build(): DSLContainer<T, R> {
        this.constructLambda?.invoke(this)
        return this
    }

    fun build(block: DSLBlock<T, R>): DSLContainer<T, R> {
        this.constructLambda?.invoke(this)
        return this
    }
    fun build(lambda: DSLContainer<T, R>.() -> Unit): DSLContainer<T, R> {
       lambda.invoke(this)
       return this
    }

    private fun <T: Any> resolveAsChild(inputData: T): List<R> {
        return dataProvider?.let { adapter ->
            val converted = adapter.trigger(inputData)
            dslBlocks.map { it.trigger(converted) }
        } ?: run {
            emptyList()
        }
    }

    fun resolve(inputData: T, adapterFn: (List<R>) -> R):R{
        val listResult = resolve(inputData)
        return adapterFn.invoke(listResult)
    }

    fun resolve(inputData: T): List<R>{
        val selfResults = dslBlocks.map { it.trigger(inputData) }
        val childResults = subContainers.flatMap { it.resolveAsChild(inputData) }
        return selfResults + childResults
    }
}

