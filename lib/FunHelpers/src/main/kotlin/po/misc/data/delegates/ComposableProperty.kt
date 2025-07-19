package po.misc.data.delegates

import po.misc.context.CTX
import po.misc.context.asContext
import po.misc.exceptions.ManagedCallSitePayload


import po.misc.reflection.classes.KSurrogate
import po.misc.reflection.properties.SourcePropertyIO
import po.misc.reflection.properties.createSourcePropertyIO
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1



class ComposableProperty<T: CTX, V: Any>(
    val surrogate: KSurrogate<T>,
    val valueClass: KClass<V>
): CTX {

    override val identity = asContext()

    val exceptionPayload: ManagedCallSitePayload = ManagedCallSitePayload.create(this)

    private var backingProperty: KProperty1<T, V>? = null
    private val property: KProperty1<T, V> get() = backingProperty.getOrManaged(exceptionPayload)
    private var propertyName: String = backingProperty?.name?:"N/A"

    private val propertyIO: SourcePropertyIO<T, V> by lazy {
       createSourcePropertyIO(surrogate.receiver, property, valueClass)
    }

    fun resolveProperty(property: KProperty<*>, thisRef:T){
        backingProperty = property.castOrManaged<KProperty1<T, V>>()
        surrogate.setSourceProperty(propertyIO)
    }

    operator fun getValue(thisRef: T, property: KProperty<*>): V{
        return propertyIO.getValue()
    }

    operator fun setValue(thisRef: T, property: KProperty<*>, value:V){
        propertyIO.setValue(value)
    }

    operator fun provideDelegate(thisRef: T, property: KProperty<*>): ComposableProperty<T, V>{
        resolveProperty(property, thisRef)
        return this
    }
}

fun <T: CTX, V: Any> propertyBinding(
    surrogate: KSurrogate<T>,
    valueClass: KClass<V>
):ComposableProperty<T, V>{

    return ComposableProperty(surrogate, valueClass)
}




