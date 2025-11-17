package po.misc.callbacks



fun<E: Enum<E>, T: Any> E.subscribe(init: CallbackPayload<E, T>.() -> Unit): CallbackPayload<E, T> {
   // return CallbackPayload<E, T>(this).apply(init)
    TODO("In refactor")
}

fun <T1: Any, T2: Any> withConverter(converter: (T1) -> T2):(T1) -> T2{
    return converter
}

fun <T : Any> wrapRawCallback(raw: (T) -> Unit): (Containable<T>) -> Unit {
    return { containable -> raw(containable.getData()) }
}
