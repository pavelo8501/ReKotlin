package po.misc.reflection.objects.builders

import po.misc.collections.StaticTypeKey
import po.misc.data.delegates.ComposableProperty
import po.misc.reflection.objects.Composed
import po.misc.reflection.objects.ObjectManager
import po.misc.reflection.objects.components.KSurrogate
import po.misc.reflection.properties.PropertyIOBase
import po.misc.reflection.properties.createPropertyIO
import po.misc.reflection.properties.toPropertyInfo
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun <E: Enum<E>, T: Composed> T.createClassSurrogate(
    key:E,
    manager: ObjectManager<E, *>
): KSurrogate<E, T> {
    val objectClass = this::class as KClass<T>
    val properties = objectClass.memberProperties.mapNotNull { kProperty ->
        kProperty.get(this)?.let { value ->
            val clazz = value::class as KClass<Any>
            val property = (kProperty as KProperty1<T, Any>).createPropertyIO(objectClass, clazz, value)
            property.provideReceiver(this)
            property
        }
    }
    val responsive = KSurrogate<E, T>(key, this, manager.hooks, properties)
    manager.addSurrogate(key, responsive)
    return responsive
}


inline fun <T: Composed, reified V: Any, E: Enum<E>> T.composable(
    dataManager: ObjectManager<E, T>
): ComposableProperty<T, V, E>{
    return ComposableProperty(this, dataManager, V::class)
}

inline fun <T: Composed, reified D: Composed,  reified V: Any, E: Enum<E>> T.composable(
    dataProperty: KMutableProperty1<D, V>,
    dataManager: ObjectManager<E, T>
): ComposableProperty<T, V, E>{

    val resultClass = V::class
    val dataClass = D::class
    val property =  dataProperty.toPropertyInfo(dataClass)
    property.returnTypeKey = StaticTypeKey.createTypeKey(resultClass)
    val default = getDefaultForType<V>(dataProperty.returnType)
    val ioProperty = property.createPropertyIO(resultClass, PropertyIOBase.PropertyType.StaticallySet, default)
    dataManager.storeProperty(ioProperty)
    return ComposableProperty(this, dataManager, V::class, ioProperty)
}