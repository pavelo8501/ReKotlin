package po.misc.configs.hocon.properties

import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.helpers.output
import po.misc.data.logging.Verbosity
import po.misc.exceptions.managedException
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


sealed class HoconDelegateBase<T: HoconResolvable<T>, V, R>(
   protected val config:  T,
   protected val entry: HoconEntryBase<T, V>,
   protected val typeToken: TypeToken<V>,
): Component,  ReadOnlyProperty<T, R> {

    val value: V get() =  entry.result
    var resolvedProperty: KProperty<*>? = null
        private set

    protected var kProperty: KProperty<*>
        get() = entry.property.getOrThrow(this){ managedException(it.message) }
        set(value) {
            resolvedProperty = value
            entry.initialize(value)
        }

    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconDelegateBase<T, V, R> {
        kProperty = property
        property.returnType.isMarkedNullable.output("KProperty on ${componentID.componentName} Nullable: ")

        return this
    }
}

class HoconProperty<T: HoconResolvable<T>, V>(
    receiver: T,
    entry: HoconEntry<T, V>,
): HoconDelegateBase<T, V, V>(receiver,  entry, entry.hoconPrimitive.typeToken){

    override val componentID: ComponentID = entry.componentID

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return value
    }
}

class HoconTransformProperty<T: HoconResolvable<T>, V: Any, R>(
    receiver:  T,
    entry: HoconEntry<T, V>,
    val hoconPrimitive: HoconPrimitives<V>,
    val transformLambda: (V)->R
): HoconDelegateBase<T, V, R>(receiver, entry,  hoconPrimitive.typeToken){

    override val componentID: ComponentID = entry.componentID

    private val valueRequest: V  get() = value.getOrThrow {
        val msg = "value accessed before initialization. ${componentID.componentName}"
        managedException(msg)
    }

    private var resultBacking: R? = null
    val result: R  get() =  resultBacking?:transformLambda(valueRequest)

    override fun getValue(thisRef: T, property: KProperty<*>): R {
        resultBacking ?:run {
            val result = transformLambda.invoke(value)
            resultBacking = result
            result
        }
       return  transformLambda.invoke(value)
    }
}


class HoconListProperty<T: HoconResolvable<T>, V>(
    receiver: T,
    val  listEntry: HoconListEntry<T, V>,
): HoconDelegateBase<T, V, List<V>>(receiver, listEntry,  listEntry.hoconList.typeToken){

    override val componentID: ComponentID = entry.componentID
    override fun getValue(thisRef: T, property: KProperty<*>): List<V> {
        return listEntry.listValue
    }
}

class HoconNestedProperty<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    receiver: T,
    val  nestedEntry: HoconNestedEntry<T, V>,
): HoconDelegateBase<T, V, V>(receiver,nestedEntry,  nestedEntry.hoconPrimitive.typeToken) {

    override val componentID: ComponentID = nestedEntry.componentID
    override fun getValue(thisRef:T, property: KProperty<*>): V {
        return nestedEntry.nestedClass
    }
}

