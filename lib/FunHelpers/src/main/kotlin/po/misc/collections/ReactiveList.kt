package po.misc.collections




class ReactiveListActions<T: Any>{

    var onAdd: ((Pair<T, Int>)-> Unit)? = null
    var onRemove: ((Int)-> Unit)? = null

    fun onAdd(callback: (Pair<T, Int>)-> Unit){
        onAdd = callback
    }
    fun onRemove(callback: (Int)-> Unit){
        onRemove = callback
    }
}

open class ReactiveList<T: Any>(): AbstractMutableList<T>(){
    internal val actions = ReactiveListActions<T>()
    private val listBacking: MutableList<T> = mutableListOf()
    override val size: Int get() = listBacking.size

    override fun get(index: Int): T {
        return listBacking[index]
    }
    override fun set(index: Int, element: T): T {
        listBacking[index] = element
        actions.onAdd?.invoke(Pair(element, listBacking.size))
        return element
    }
    override fun removeAt(index: Int): T {
        val removed = listBacking.removeAt(index)
        actions.onRemove?.invoke(listBacking.size)
        return  removed
    }
    override fun add(index: Int, element: T) {
        listBacking.add(index, element)
        actions.onAdd?.invoke(Pair(element, listBacking.size))
    }
    override fun clear() {
        listBacking.clear()
        actions.onRemove?.invoke(0)
    }
}

fun <T: Any> reactiveListOf(
    actionConfig: (ReactiveListActions<T>.()-> Unit)? = null,
):ReactiveList<T>{
    val list = ReactiveList<T>()
    actionConfig?.invoke(list.actions)
    return list
}