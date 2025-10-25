package po.misc.types.token

import po.misc.context.tracable.TraceableContext
import kotlin.reflect.KClass

interface TokenFactory


@Deprecated("Change to tokenOf")
inline fun <reified T> TokenFactory.typeToken(): TypeToken<T> = tokenOf<T>()

inline fun <reified T> TokenFactory.tokenOf(): TypeToken<T>{
    return TypeToken.create<T>()
}


inline fun <reified T, reified GT: T> TokenFactory.preciseTokenOf(): TypeToken<T>{
    return TypeToken.createPrecise<T, GT>()
}


inline fun <T, reified GT: T> TokenFactory.tokenOf(baseClass: KClass<T & Any>): TypeToken<T>{
  return TypeToken.create<T, GT>(baseClass)
}


inline fun <reified T: TraceableContext> T.toToken(): TypeToken<T>{
    return TypeToken.create<T>()
}


inline fun <reified T: TraceableContext, reified GT: T> GT.toPreciseToken(): TypeToken<T>{
    return TypeToken.create<T>()
}

