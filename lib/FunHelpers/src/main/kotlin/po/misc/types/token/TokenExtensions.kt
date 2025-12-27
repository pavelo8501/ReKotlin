package po.misc.types.token


import po.misc.types.castOrThrow
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType


@Suppress("UNCHECKED_CAST")
fun <T> TypeToken<T>.asListType(): TypeToken<List<T>>{
    val listKClass = List::class
    val listKType = listKClass.createType(
        arguments = listOf(
            KTypeProjection.invariant(this.kType) // keep nullability & generic info
        ),
        nullable = false
    )
    return TypeToken(listKClass as KClass<List<T>>, listKType)
}

/**
 * Returns a list-typed representation of this token suitable for
 * value resolution.
 *
 * If this token already represents a collection type, it is returned
 * unchanged. Otherwise, a list type wrapping this token is produced.
 *
 * This method is intended for resolution and rendering logic where
 * both single and collection values are treated uniformly.
 */
@Suppress("UNCHECKED_CAST")
fun <T> TypeToken<T>.asEffectiveListType(): TypeToken<List<T>>{
  return  if(isCollection){
        this as TypeToken<List<T>>
    }else{
        asListType()
    }
}


@Suppress("UNCHECKED_CAST")
fun <T> TypeToken<List<T>>.asElementType(): TypeToken<T> {
    return typeSlots.firstOrNull()?.token?.castOrThrow<TypeToken<T>>()?:run {
        error("List token has no generic type slot")
    }
}