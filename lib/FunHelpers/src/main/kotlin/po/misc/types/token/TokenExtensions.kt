package po.misc.types.token


import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType


@Suppress("UNCHECKED_CAST")
fun <T> TypeToken<T>.asList(): TypeToken<List<T>>{
    val listKClass = List::class
    val listKType = listKClass.createType(
        arguments = listOf(
            KTypeProjection.invariant(this.kType) // keep nullability & generic info
        ),
        nullable = false
    )
    return TypeToken(
        kClass = listKClass as KClass<List<T>>,
        kType = listKType
    )
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypeToken<List<T>>.asElement(): TypeToken<T> {
    if(!tokenData.isCollection){
        error("Not a list TypeToken")
    }
    val slot = typeSlots.getOrNull(0)
        ?: error("List token has no generic type slot")
    return TypeToken(
        kClass = slot.kClass as KClass<T>,
        kType = slot.kType,
    )
}