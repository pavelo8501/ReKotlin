package po.misc.callbacks



fun <T: Any> callbackManager(): CallbackManager<T, Unit, CallbackPayload<T>> =
    CallbackManager<T, Unit,  CallbackPayload<T>>()


fun <T: Any, R: Any> returnCallbackManager(): CallbackManager<T, R, ResultCallbackPayload<T, R>> =
    CallbackManager<T, R, ResultCallbackPayload<T, R>>()