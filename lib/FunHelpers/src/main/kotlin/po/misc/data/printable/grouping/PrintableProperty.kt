package po.misc.data.printable.grouping

import po.misc.collections.ReactiveList
import po.misc.data.printable.Printable
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KProperty

class PrintableProperty<M: Printable>(
    val host: Printable,
    private var nameProvider: () -> String,
    var onNewAdd: (PrintableProperty<M>.(M) -> Unit)? = null
): AbstractList<M>(){

    private val recordsBacking: ReactiveList<M> = ReactiveList{
        onNewAdd?.invoke(this,  it)
    }

    override val size: Int get() = recordsBacking.size

    val name: String get() = nameProvider()

    constructor(host: Printable, name: String, onNewAdd: (PrintableProperty<M>.(M) -> Unit)? = null): this(host, {  name } , onNewAdd)


    fun add(data: M):M{
        recordsBacking.add(data)
        return data
    }

    fun clear(){
        recordsBacking.clear()
    }

    override fun get(index: Int): M {
       return recordsBacking[index]
    }

    operator fun provideDelegate(
        thisRef: Printable,
        property: KProperty<*>,
    ): PrintableProperty<M> {

        nameProvider = {
            property.name
        }

        return this
    }

    operator fun getValue(
        thisRef: Printable,
        property: KProperty<*>,
    ): MutableList<M> {
        return recordsBacking
    }

}


fun <T: Printable> Printable.createProperty(name: String):PrintableProperty<T> =printableProperty(name)

fun <T: Printable> Printable.printableProperty(name: String):PrintableProperty<T>{
    return PrintableProperty<T>(this, name)
}

inline fun <reified M: Printable> Printable.createProperty(
    noinline onNew: PrintableProperty<M>.(M) -> Unit
):PrintableProperty<M> = printableProperty(onNew)


inline fun <reified M: Printable> Printable.printableProperty(
    noinline onNew: PrintableProperty<M>.(M) -> Unit
):PrintableProperty<M>{
    return PrintableProperty<M>(this, { M::class.simpleOrAnon }, onNew )
}
