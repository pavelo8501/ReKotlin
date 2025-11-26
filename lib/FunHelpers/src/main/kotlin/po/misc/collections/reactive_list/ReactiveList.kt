package po.misc.collections.reactive_list

import po.misc.collections.lambda_map.CallableWrapper
import po.misc.collections.lambda_map.Lambda
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.ClassResolver
import kotlin.collections.forEach


open class ReactiveList<T: Any, R>(
    val options: Options? = null,
    val onNewEntry: CallableWrapper<T, Unit>,
    vararg lambdas : CallableWrapper<T, R>
): AbstractMutableList<T>() {

    data class Options(
        val host: TraceableContext,
        var disableSideEffects: Boolean = false,
        var initialName:String = ""
    ){
        private val formattedClassName: String = ClassResolver.classInfo(ReactiveList::class).formattedClassName
        val name: String = "$formattedClassName on ${ClassResolver.instanceName(host)}"
    }

    constructor(options: Options?, lambda: (T) -> Unit):this(options, Lambda(lambda))


    var sideEffects: Boolean
        get() = options?.disableSideEffects?:false
        set(value) {
            if(options != null){
                options.disableSideEffects = value
            }

        }

    private var nameProvider: (() -> String)? = null
    internal val recordsBacking = mutableListOf<T>()

    override val size: Int get() = recordsBacking.size

    val name: String get() = nameProvider?.invoke() ?: options?.name?:"ReactiveList"



    protected open fun newEntry(data: T, noSideEffects: Boolean){
        if (!sideEffects && !noSideEffects) {
            onNewEntry.invoke(data)
        }
    }

    override fun get(index: Int): T {
        return recordsBacking[index]
    }

    fun add(data: T, noSideEffects: Boolean) {
        newEntry(data, noSideEffects)
        recordsBacking.add(data)
    }

    override fun add(index: Int, element: T) {
        newEntry(element, noSideEffects = false)
        recordsBacking.add(index, element)
    }

    fun set(index: Int, element: T, noSideEffects: Boolean): T {
        newEntry(element, noSideEffects)
        return recordsBacking.set(index, element)
    }

    override fun set(index: Int, element: T): T {
        newEntry(element, noSideEffects = false)
        return recordsBacking.set(index, element)
    }

    fun addAll(elements: Collection<T>, noSideEffects: Boolean): Boolean {
        elements.forEach {
            add(it, noSideEffects)
        }
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return addAll(elements, noSideEffects = false)
    }

    override fun removeAt(index: Int): T {
        return recordsBacking.removeAt(index)
    }

    override fun clear() {
        recordsBacking.clear()
    }

}