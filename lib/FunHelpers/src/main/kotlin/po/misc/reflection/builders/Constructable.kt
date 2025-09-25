package po.misc.reflection.builders

import po.misc.reflection.anotations.AnnotationContainer
import po.misc.reflection.anotations.annotatedProperties
import po.misc.types.castOrManaged
import kotlin.reflect.KClass


interface Constructable<T: Any>{
    val builder:()->T
}

abstract class ConstructableClass<T: Any> (

) :Constructable<T> {

    abstract override val builder: () -> T

    var containerBacking: AnnotationContainer<T, Annotation>? = null

    inline fun <reified A: Annotation> buildPropertyContainer(readValues: Boolean = false):AnnotationContainer<T, A> {
        if (containerBacking == null) {
            val receiver =  builder()
            val container = if(!readValues){
                val kClass = receiver::class
                kClass.annotatedProperties<T, Annotation>(null)
            }else{
                annotatedProperties<Any, Annotation>(receiver)
            }
            containerBacking = container.castOrManaged<AnnotationContainer<T, Annotation>>(this)
        }
        return containerBacking.castOrManaged<AnnotationContainer<T, A>>(this)
    }

    open fun build(): T {
        return builder.invoke()
    }
}


