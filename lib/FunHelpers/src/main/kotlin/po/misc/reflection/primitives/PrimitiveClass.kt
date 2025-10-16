package po.misc.reflection.primitives

import po.misc.context.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.managedException
import po.misc.types.getOrThrow
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.safeCast
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Duration

sealed interface PrimitiveClass<T: Any>: TraceableContext{
    val kClass: KClass<T>

    companion object: TraceableContext {
        val all: List<PrimitiveClass<*>> =
            listOf(
                StringClass, IntClass, LongClass, BooleanClass, DoubleClass,
                DurationClass, LocalDateTimeClass
            )

         val byClass: Map<KClass<out Any>, PrimitiveClass<*>> = all.associateBy { it.kClass }

        fun ofClass(kClass: KClass<*>?): PrimitiveClass<*>? = byClass[kClass]
    }
}

inline fun <reified T: Any> PrimitiveClass.Companion.lookupPrimitive():PrimitiveClass<T>{
    val thisClass = T::class
    "PrimitiveClass lookup".output(Colour.Green)
    val casted =  PrimitiveClass.Companion.byClass[thisClass]?.let {
        "PrimitiveClass found for ${thisClass.simpleOrAnon} casting".output(Colour.Green)
        it.safeCast<PrimitiveClass<T>>()
    }
    return casted.getOrThrow(this){
        "Cast failed".output(Colour.Red)
        managedException("Safe cast returned null")
    }
}

object WildCardClass: PrimitiveClass<Any>{
    override val kClass: KClass<Any> = Any::class
}

object StringClass: PrimitiveClass<String>{
    override val kClass: KClass<String> = String::class
}

object IntClass: PrimitiveClass<Int>{
    override val kClass: KClass<Int> = Int::class
}

object LongClass: PrimitiveClass<Long>{
    override val kClass: KClass<Long> = Long::class
}

object BooleanClass: PrimitiveClass<Boolean>{
    override val kClass: KClass<Boolean> = Boolean::class
}

object DoubleClass : PrimitiveClass<Double> {
    override val kClass: KClass<Double> = Double::class
}

object DurationClass : PrimitiveClass<Duration> {
    override val kClass: KClass<Duration> = Duration::class
}

object LocalDateTimeClass : PrimitiveClass<LocalDateTime> {
    override val kClass: KClass<LocalDateTime> = LocalDateTime::class
}








