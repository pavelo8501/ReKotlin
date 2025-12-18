package po.misc.callbacks.context_signal

import po.misc.functions.NoParam
import po.misc.functions.NoResult
import po.misc.types.token.TypeToken


@PublishedApi
internal fun <T, T1, R> createContextSignal(
    type: TypeToken<T>,
    parameterType:TypeToken<T1>,
    resultType:TypeToken<R>,
    opt: CTXSignalOpt? = null,
): ContextSignal<T, T1, R> {
    val event = ContextSignal(type, parameterType, resultType)
    if(opt != null){
        event.applyOptions(opt)
    }
    return event
}


inline fun <reified T, reified T1, reified R> contextSignalOf(
    options: CTXSignalOpt? = null,
): ContextSignal<T, T1, R> = createContextSignal(TypeToken<T>(), TypeToken<T1>(), TypeToken<R>(), options)



inline fun <reified T, reified R> contextSignalOf(
    noParam : NoParam,
    options: CTXSignalOpt? = null
): ContextSignal<T, Unit,  R> = createContextSignal(TypeToken<T>(), TypeToken<Unit>(), TypeToken<R>(), options)

inline fun <reified T, reified T1> contextSignalOf(
    noResult: NoResult,
    options: CTXSignalOpt? = null,
): ContextSignal<T, T1, Unit> = createContextSignal(TypeToken<T>(), TypeToken<T1>(), TypeToken<Unit>(), options)

fun <T, T1> contextSignalOf(
    type: TypeToken<T>,
    parameterType:TypeToken<T1>,
    options: CTXSignalOpt? = null,
): ContextSignal<T, T1, Unit> = createContextSignal(type, parameterType, TypeToken<Unit>(), options)


inline fun <reified T> contextSignalOf(
    noParam : NoParam,
    noResult: NoResult,
    options: CTXSignalOpt? = null,
): ContextSignal<T, Unit, Unit> = createContextSignal(TypeToken<T>(), TypeToken<Unit>(), TypeToken<Unit>(), options)


fun <T> contextSignalOf(
    type: TypeToken<T>,
    options: CTXSignalOpt? = null,
): ContextSignal<T, Unit, Unit> = createContextSignal(type, TypeToken<Unit>(), TypeToken<Unit>(), options)






