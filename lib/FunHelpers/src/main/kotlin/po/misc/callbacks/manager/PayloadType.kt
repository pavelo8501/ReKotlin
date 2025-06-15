package po.misc.callbacks.manager

sealed class PayloadType<T: Any, R: Any> {

    class SimplePayload<T: Any>() : PayloadType<T, Unit>()
    class WithResultPayload<T: Any, R: Any>() : PayloadType<T, R>()


}