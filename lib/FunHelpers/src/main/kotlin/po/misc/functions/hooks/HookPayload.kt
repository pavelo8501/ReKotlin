package po.misc.functions.hooks

interface ErrorPayload<T: Any> {

    val throwable: Throwable
    val snapshot: String
}
