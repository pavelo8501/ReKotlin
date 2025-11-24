package po.misc.types

import po.misc.collections.asList
import po.misc.types.k_class.computeHierarchy
import kotlin.reflect.KClass

class ClassHierarchyMap(
    val fromClass: KClass<*>,
    val maxDistance: Int = 0,
    val stopBefore: KClass<*> = Any::class
) {
    var hierarchyCache: List<KClass<*>> = listOf()
        internal set

    init {
        resolve()
    }
    fun resolve(): List<KClass<*>>{
        if(maxDistance <= 0){
            hierarchyCache = fromClass.asList()
            return hierarchyCache
        }else{
            hierarchyCache = fromClass.computeHierarchy(maxDistance, stopBefore)
            return hierarchyCache
        }
    }
    fun getClassDistance(kClass: KClass<*>): Int{
       return  hierarchyCache.indexOfFirst { it == kClass }
    }
}
