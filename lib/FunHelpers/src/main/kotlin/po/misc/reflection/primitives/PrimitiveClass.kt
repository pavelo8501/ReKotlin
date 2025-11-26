package po.misc.reflection.primitives

import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.managedException
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Duration

sealed interface PrimitiveClass<T>: TraceableContext, TokenFactory{
    val typeToken: TypeToken<T>
   // val kClass: KClass<T>

    companion object: TraceableContext {
        val all: List<PrimitiveClass<*>> =
            listOf(
                StringClass, IntClass, LongClass, BooleanClass, DoubleClass,
                DurationClass, LocalDateTimeClass
            )

         val byClass: Map<KClass<out Any>, PrimitiveClass<*>> = all.associateBy { it.typeToken.kClass }

        fun ofClass(kClass: KClass<*>?): PrimitiveClass<*>? = byClass[kClass]
    }
}

inline fun <reified T: Any> PrimitiveClass.Companion.lookupPrimitive():PrimitiveClass<T>{
    val thisClass = T::class
    val casted = PrimitiveClass.byClass[thisClass]?.safeCast<PrimitiveClass<T>>()
    return casted.getOrThrow(this){
        managedException("Safe cast returned null")
    }
}

abstract class WildCardClass<T>(
    override val typeToken: TypeToken<T>
): PrimitiveClass<T>{


}

object NullClass{
    val kClass: KClass<NullClass> = NullClass::class
}

object StringClass: PrimitiveClass<String>{
    override val typeToken: TypeToken<String> = tokenOf()
    val kClass: KClass<String> = typeToken.kClass
}

object IntClass: PrimitiveClass<Int>{
    override val typeToken: TypeToken<Int> = tokenOf()
    val kClass: KClass<Int> = typeToken.kClass
}

object LongClass: PrimitiveClass<Long>{
    override val typeToken: TypeToken<Long> = tokenOf()
    val kClass: KClass<Long> = typeToken.kClass
}

object BooleanClass: PrimitiveClass<Boolean>{
    override val typeToken: TypeToken<Boolean> = tokenOf()
    val kClass: KClass<Boolean> = typeToken.kClass
}

object DoubleClass : PrimitiveClass<Double> {
    override val typeToken: TypeToken<Double> = tokenOf()
    val kClass: KClass<Double> = typeToken.kClass
}

object DurationClass : PrimitiveClass<Duration> {
    override val typeToken: TypeToken<Duration> = tokenOf()
    val kClass: KClass<Duration> = typeToken.kClass
}

object LocalDateTimeClass : PrimitiveClass<LocalDateTime> {
    override val typeToken: TypeToken<LocalDateTime> = tokenOf()
    val kClass: KClass<LocalDateTime> = typeToken.kClass
}









