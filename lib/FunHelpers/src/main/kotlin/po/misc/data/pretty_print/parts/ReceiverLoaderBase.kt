package po.misc.data.pretty_print.parts

import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.pretty_print.grid.PrettyPromiseGrid
import po.misc.data.styles.Colour
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


sealed class ReceiverLoaderBase<T: Any,  V>(
): TraceableContext {

    protected var  propertyBacking: KProperty1<T, V>? = null

    open val  property: KProperty1<T, V> get() {
        return propertyBacking.getOrThrow(KProperty1::class)
    }

    protected var  readOnlyPropertyBacking: KProperty1<T, V>? = null
    val  readOnlyProperty: KProperty1<T, V> get() {
        return propertyBacking.getOrThrow(KProperty1::class)
    }

    internal var providerBacking: (() -> Collection<V>)? = null
    val provider: () -> Collection<V> get() {
        return providerBacking.getOrThrow(this)
    }
}

class ReceiverLoader<T1: Any>(): ReceiverLoaderBase<Any, Collection<T1>>() {
    fun provideCollectionProperty(property:  KProperty1<Any, Collection<T1>>){
        propertyBacking = property
    }
}


//class TypedReceiverLoader<T: Any, V: Any>(
//    val typeToken: TypeToken<V>,
//): ReceiverLoaderBase<Any, Collection<V>>() {
//    fun provideCollectionProperty(property:  KProperty1<Any, Collection<T1>>){
//        propertyBacking = property
//    }
//}


/**
 * Loads a collection property from a receiver of type [T] and invokes the template builder
 * for generating grid rows.
 *
 * This loader is responsible for:
 *  - storing a reference to the collection property (`KProperty1<T, Collection<V>>`)
 *  - converting the collection into a plain `List<V>`
 *  - invoking the grid’s template builder with the extracted list
 *
 * It abstracts away receiver → list extraction so that [PrettyPromiseGrig] can focus
 * solely on row rendering logic.
 *
 * @param typeToken Reflection token for the element type.
 * @param property The collection property to be read from the receiver.
 */
class ReceiverListLoader<T: Any, V: Any>(
    val typeToken: TypeToken<V>,
    override val  property: KProperty1<T,  Collection<V>>,
): ReceiverLoaderBase<T, Collection<V>>() {

    private var builderBacking: (PrettyPromiseGrid<T, V>.(List<V>) -> Unit)? = null
    val builder: PrettyPromiseGrid<T, V>.(List<V>) -> Unit get() {
        return builderBacking.getOrThrow(this)
    }
    init {
        provideReadOnlyProperty(property)
    }

    val journal = mutableListOf<Pair<String, Boolean>>()

    fun provideReadOnlyProperty(
        property: KProperty1<T,  Collection<V>>,
    ){
        propertyBacking = property
    }
    fun provideBuilder(
        builder:  PrettyPromiseGrid<T, V>.(List<V>)-> Unit
    ){
        builderBacking = builder
    }

    fun resolveReceiver(receiver:T): List<V> {
        return property.get(receiver).toList()
    }

    fun <T1: Any> tryResolveReceiver(receiver: T1): List<V>? {

      return  property.safeCast<KProperty1<T1,  Collection<V>>>()?.let {casted->
            casted.get(receiver).toList()
      }?:run {
          "tryResolveReceiver. Property receiver of wrong type".output(Colour.Yellow)
          journal.add(Pair("tryResolveReceiver. Property receiver of wrong type", false))
          null
      }

    }

    fun applyConfiguration(builderReceiver: PrettyPromiseGrid<T, V>, parameter: List<V>){
        builderBacking?.let {
            it.invoke(builderReceiver, parameter)
            journal.add(Pair("applyConfiguration", true))
        }?:run {
            journal.add(Pair("applyConfiguration. Failed builder lambda not provided", false))
        }
    }

    fun resolveTemplate(
        receiver:T,
        builderReceiver: PrettyPromiseGrid<T, V>
    ): List<V>{
       val resultList = resolveReceiver(receiver)
       applyConfiguration(builderReceiver, resultList)
        return resultList
    }

}
