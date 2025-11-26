package po.misc.data.printable.grouping

import po.misc.data.printable.Printable
import kotlin.reflect.KProperty


class PrintableProperty<T: Printable>(
    val host: Printable,
    var initialName:String = "",
    internal var onSubEntry: (PrintableProperty<T>.(T) -> Unit)? = null
): AbstractMutableList<T>() {

    var disableSideEffects: Boolean = false

    private var nameProvider: (() -> String)? = null
    internal val recordsBacking = mutableListOf<T>()

    override val size: Int get() = recordsBacking.size

    val name: String get() = nameProvider?.invoke() ?: initialName

    private fun newEntry(data: T, noSideEffects: Boolean){
        if (!disableSideEffects && !noSideEffects) {
            onSubEntry?.invoke(this, data)
        }
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

    override fun clear() {
        recordsBacking.clear()
    }

    override fun get(index: Int): T {
        return recordsBacking[index]
    }

    override fun removeAt(index: Int): T {
        return recordsBacking.removeAt(index)
    }

    operator fun provideDelegate(
        thisRef: Printable,
        property: KProperty<*>,
    ): PrintableProperty<T> {
        if (initialName.isBlank()) {
            initialName = property.name
        }
        return this
    }

    operator fun getValue(
        thisRef: Printable,
        property: KProperty<*>,
    ): PrintableProperty<T> {
        return this
    }
}

fun <T: Printable> Printable.printableProperty(
    useName: String? = null
):PrintableProperty<T>{
  return  useName?.let {name->
        PrintableProperty<T>(this, name)
    }?:run {
        PrintableProperty<T>(this)
    }
}

inline fun <reified T: Printable> Printable.printableProperty(
    useName: String? = null,
    noinline onSubEntry: PrintableProperty<T>.(T) -> Unit
):PrintableProperty<T>{
   return useName?.let {name->
       PrintableProperty(this, name,  onSubEntry )
    }?:run {
        PrintableProperty(this, onSubEntry = onSubEntry )
    }
}
