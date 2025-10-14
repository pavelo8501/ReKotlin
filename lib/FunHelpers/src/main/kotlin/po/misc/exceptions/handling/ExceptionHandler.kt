package po.misc.exceptions.handling


sealed interface ExceptionHandler<in TH : Throwable>

fun interface RecoveringHandler<in TH : Throwable> : ExceptionHandler<TH> {
    fun handle(exception: TH)
}

fun interface ThrowingHandler<in TH : Throwable> : ExceptionHandler<TH> {
    fun handleAndThrow(exception: TH): Nothing
}

fun interface TransformingHandler<in TH : Throwable, out R> : ExceptionHandler<TH> {
    fun handleAndTransform(exception: TH): R
}
