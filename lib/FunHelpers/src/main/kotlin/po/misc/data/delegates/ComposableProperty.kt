package po.misc.data.delegates

import po.misc.data.anotation.Composable
import po.misc.reflection.objects.Composed
import po.misc.reflection.objects.ObjectManager
import po.misc.reflection.properties.PropertyIO
import po.misc.reflection.properties.SourcePropertyIO
import po.misc.reflection.properties.createPropertyIO
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


@Composable()
class ComposableProperty<T: Composed, V: Any, E: Enum<E>>(
    val holdingObject: T,
    val dataManager: ObjectManager<E, T>,
    val valueClass: KClass<V>,
    vararg val vararg :  PropertyIO<*, V>
) {
    internal val value:V get() = ioProperty.currentValue
    private var backingProperty: SourcePropertyIO<T, V>? = null
    val ioProperty: SourcePropertyIO<T, V> by lazy {
        backingProperty.getOrManaged("backingProperty of ${holdingObject::class.simpleName}")
    }
    var propertyName: String = backingProperty?.propertyName?:"N/A"

    fun resolveProperty(property: KProperty<*>, thisRef:T){
        if(backingProperty == null){
            val default =  holdingObject.getDefaultForType<V>(property.returnType)
            dataManager.setSource(thisRef)
            val casted = property.castOrManaged<KProperty1<T, V>>()
            backingProperty = createPropertyIO(thisRef, casted, valueClass, default)
            vararg.forEach {
                ioProperty.attachAuxDataProperty(it)
            }
            dataManager.sourceClass.put(ioProperty.propertyName, ioProperty.castOrManaged())
        }
    }

    operator fun getValue(thisRef: T, property: KProperty<*>): V{
       return value
    }

    operator fun setValue(thisRef: T, property: KProperty<*>, value:V){
        resolveProperty(property, thisRef)
        ioProperty.provideReceiver(thisRef)
        ioProperty.setValue(value)
    }

    operator fun provideDelegate(thisRef: T, property: KProperty<*>): ComposableProperty<T, V, E>{
        resolveProperty(property, thisRef)
        return this
    }
}


