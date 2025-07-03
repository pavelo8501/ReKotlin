package po.misc.data.delegates

import po.misc.collections.StaticTypeKey
import po.misc.data.anotation.Composable
import po.misc.reflection.objects.Composed
import po.misc.reflection.properties.PropertyIO
import po.misc.reflection.properties.SourcePropertyIO
import po.misc.reflection.properties.createPropertyIO
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface ValueBasedEntry: Comparable<Long>{
    val objectId : Long
    fun updateId(id: Long)
}

interface ComposableCollection<T: Any, V: ValueBasedEntry>{
    val hostingObject:T
    fun inputData(data: V)
    fun collectData(objectId: Long):V?
    fun initializeForeignClass()
}


//
//abstract class ComposableMap<T: Any, V: ValueBasedEntry>(
//    override val hostingObject:T
//):AbstractMutableMap<Long, V>(), ComposableCollection<T, V> {
//
//    val backingMap: MutableMap<Long, V> = mutableMapOf()
//
//    override val entries: MutableSet<MutableMap.MutableEntry<Long, V>> get(){
//        return backingMap.entries
//    }
//
//    override fun inputData(data: V){
//        backingMap[data.objectId] = data
//    }
//
//    final override fun collectData(objectId: Long):V?{
//        return backingMap[objectId]
//    }
//
//    override fun put(key: Long, value: V): V? {
//        return  backingMap.put(key, value)
//    }
//
//}