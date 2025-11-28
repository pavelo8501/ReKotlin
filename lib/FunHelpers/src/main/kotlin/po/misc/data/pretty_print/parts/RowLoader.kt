package po.misc.data.pretty_print.parts

import po.misc.context.tracable.TraceableContext
import po.misc.types.getOrThrow
import kotlin.reflect.KProperty1

class RowLoader<T1: Any>(

): TraceableContext {


    internal var  propertyBacking: KProperty1<Any, Collection<T1>>? = null
    val  property: KProperty1<Any, Collection<T1>> get() {
        return propertyBacking.getOrThrow(KProperty1::class)
    }
    internal var providerBacking: (() -> Collection<T1>)? = null
    val provider: () -> Collection<T1> get() {
        return providerBacking.getOrThrow(this)
    }

    fun provideCollectionProperty(property:  KProperty1<Any, Collection<T1>>){
        propertyBacking = property
    }


}