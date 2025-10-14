package po.misc.reflection.primitives

import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Duration

sealed interface PrimitiveClass<T: Any>{
    val kClass: KClass<T>

    companion object {
        val all: List<PrimitiveClass<*>> =
            listOf(
                StringClass, IntClass, LongClass, BooleanClass, DoubleClass,
                DurationClass, LocalDateTimeClass
            )

        val byClass: Map<KClass<out Any>, PrimitiveClass<*>> = all.associateBy { it.kClass }

        fun ofClass(kClass: KClass<*>?): PrimitiveClass<*>? = byClass[kClass]
    }
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







