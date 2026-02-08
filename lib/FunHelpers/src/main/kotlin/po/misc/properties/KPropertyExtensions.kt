package po.misc.properties

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf


val <V> KProperty<V>.isReturnTypeList : Boolean get (){
    val kClass = returnType.classifier as? KClass<*>
    return kClass?.isSubclassOf(List::class)?: false
}