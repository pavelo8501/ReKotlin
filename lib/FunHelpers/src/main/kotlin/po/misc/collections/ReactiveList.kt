package po.misc.collections


open class ReactiveList<T: Any>(
    val onEvery: (T)-> Unit
): AbstractMutableList<T>(){

    private val listBacking: MutableList<T> = mutableListOf()
    override val size: Int get() = listBacking.size

    override fun get(index: Int): T {
        return listBacking[index]
    }
    override fun set(index: Int, element: T): T {
        listBacking[index] = element
        onEvery.invoke(element)
        return element
    }
    override fun removeAt(index: Int): T {
        return  listBacking.removeAt(index)
    }
    override fun add(index: Int, element: T) {
        listBacking.add(index, element)
        onEvery.invoke(element)
    }
}

fun <T: Any> reactiveListOf(
    onEvery: (T)-> Unit
):ReactiveList<T>{
    return ReactiveList(onEvery)
}