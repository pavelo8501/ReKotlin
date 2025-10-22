package po.misc.callbacks.validator

interface ValidationProvider<T: Any> {
    fun validate(data: T): Boolean
}