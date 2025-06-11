package po.misc.callbacks



fun <T: Any> callbackManager(): CallbackManager<T, Unit> =
    CallbackManager<T, Unit,>()


fun <T: Any, R: Any> resultCallbackManager(): CallbackManager<T, R> =
    CallbackManager<T, R>()