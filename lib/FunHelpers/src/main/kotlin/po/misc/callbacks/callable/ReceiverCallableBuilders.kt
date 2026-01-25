package po.misc.callbacks.callable

import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


inline fun <reified T, reified R> T.asFunCallable(noinline function: Function1<T, R>): FunctionCallable<T, R> {
    return FunctionCallable<T,R>(function)
}

inline fun <reified T> T.asProvider():ProviderCallable<T, T> {
    val typeToken = TypeToken<T>()
    val provider = { this }
    return ProviderCallable(typeToken, typeToken, provider)
}

inline fun <reified T, reified V> T.asProvider(noinline provider : () ->  V):ProviderCallable<T, V> {
    return ProviderCallable(TypeToken<T>(), TypeToken<V>(),  provider)
}

inline fun <reified T, reified R> KProperty1<T, R>.toCallable(): PropertyCallable<T, R> {
    return PropertyCallable<T,R>(this)
}
inline fun <T, reified R> KProperty1<T, R>.toCallable(sourceType: TypeToken<T>): PropertyCallable<T, R> {
    return PropertyCallable<T, R>(sourceType, this)
}