package po.misc.reflection

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.KClass

interface MetaContainer{
    companion object{
        val snapshot: MutableMap<String, KProperty1<*, Any?>> = mutableMapOf()
    }
}
//
//inline fun <reified T:MetaContainer> T.LogOnFault(block: (T.()-> Unit)):T{
//    this::class.memberProperties.forEach {
//        MetaContainer.snapshot[it.name] = it
//    }
//    block.invoke(this).apply {
//        this::class.memberProperties.forEach {
//            MetaContainer.snapshot[it.name] = it
//        }
//    }
//    return this
//}


inline fun <reified T : Any> KClass<*>.findPropertiesOfType(): List<KProperty1<Any, T>> {
    @Suppress("UNCHECKED_CAST")
    return this.memberProperties
        .filter { prop -> prop.returnType.jvmErasure == T::class }
        .mapNotNull {
            it  as? KProperty1<Any, T>
        }
}