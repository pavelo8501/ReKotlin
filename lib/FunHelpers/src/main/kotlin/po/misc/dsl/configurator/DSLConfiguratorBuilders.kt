package po.misc.dsl.configurator

import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import kotlin.enums.enumEntries
import kotlin.reflect.KClass


inline fun <T: TraceableContext, E> T.configurator(
    enumClass: Class<E>,
    block : DSLConfiguratorBuilder<T>.()-> Unit
): DSLConfigurator<T> where E : HasNameValue, E : Enum<E>{
   val configurator= configurator(enumClass)
   configurator.block()
   return configurator
}

fun <T: TraceableContext, E> T.configurator(enumClass: Class<E>): DSLConfigurator<T> where E : HasNameValue, E : Enum<E>{
    val enums  = enumClass.enumConstants ?: emptyArray()
    val dslGroups = enums.map {
        DSLGroup<T>(it)
    }
    return DSLConfigurator(dslGroups)
}


inline fun <T: TraceableContext> T.configurator(
    block : DSLConfiguratorBuilder<T>.()-> Unit
): DSLConfigurator<T>{
    val configurator = configurator<T>()
    configurator.block()
    return configurator
}

fun <T: TraceableContext> T.configurator(): DSLConfigurator<T>{
    return DSLConfigurator<T>()
}