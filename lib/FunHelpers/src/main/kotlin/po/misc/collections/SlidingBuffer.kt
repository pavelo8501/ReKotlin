package po.misc.collections

import java.time.Instant


abstract class BufferItem<T: Any>(){
    abstract val value:T
    val created : Instant = Instant.now()
}

class SlidingBuffer<T : BufferItem<V>, V: Any>(
    private val capacity: Int,
    private val itemProvider:(V)-> T
) {
    private val buffer = mutableListOf<T>()
    private  val recentItem:T? get() = get(0)
    val size: Int get() = buffer.size

    private fun removeIfExceeded(){
        if (buffer.size == capacity) {
            buffer.removeLast()
        }
    }

    fun add(data: V) {
        removeIfExceeded()
        val item = itemProvider.invoke(data)
        buffer.add(0, item)
    }

    fun addItem(item: T) {
        removeIfExceeded()
        buffer.add(0, item)
    }

    fun addIfDifferent(data: V, onDifferent:(V)-> Unit){
        val item = itemProvider.invoke(data)
        if(item.value != recentItem?.value){
            addItem(item)
            onDifferent.invoke(data)
        }
    }

    fun toList(): List<T> = buffer.toList()

    fun getValue():V?{
        return recentItem?.value
    }

    override fun toString(): String {
      return  buffer.joinToString("->") { "${it.value} @ ${it.created}" }
    }

    operator fun get(index: Int):T? {
        return buffer.getOrNull(index)
    }

}