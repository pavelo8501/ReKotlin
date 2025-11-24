package po.misc.reflection

import kotlin.reflect.KClass
import kotlin.reflect.KProperty


fun  KProperty<*>.returnClass(): KClass<*>?{
    val kClass = returnType.classifier as? KClass<*>
    return kClass
}
