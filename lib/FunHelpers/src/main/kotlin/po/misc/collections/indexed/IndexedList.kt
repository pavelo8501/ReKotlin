package po.misc.collections.indexed


class IndexedList<T:Indexed>(

) : AbstractMutableList<T>() {

    internal val listBacking = mutableListOf<T>()

    override val size: Int get() = listBacking.size

    private fun reindex() {
        val totalSize = size
        listBacking.forEachIndexed { index, element ->
            element.setIndex(index, totalSize)
        }
    }
    
    override fun get(index: Int): T = listBacking[index]

    override fun add(index: Int, element: T) {
        listBacking.add(index, element)
        reindex()
    }

    override fun set(index: Int, element: T): T {
        val old = listBacking.set(index, element)
        reindex()
        return old
    }

    override fun removeAt(index: Int): T {
        val removed = listBacking.removeAt(index)
        reindex()
        return removed
    }
}