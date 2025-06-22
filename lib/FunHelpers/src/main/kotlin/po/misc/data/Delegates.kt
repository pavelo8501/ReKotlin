package po.misc.data


class SmartLazy<T : Any>(
    private val defaultValue: T,
    private val lazyProvider: () -> T?,
) : Lazy<T> {

    @Volatile
    private var _value: Any? = UNINITIALIZED

    override val value: T
        get() {
            if (_value === UNINITIALIZED) {
                val current = lazyProvider()
                if (current != null) {
                    _value = current
                } else {
                    return defaultValue
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED

    companion object {
        private object UNINITIALIZED
    }
}

fun <T : Any> smartLazy( default:T, lazyProvider: () -> T?): Lazy<T> {
    return SmartLazy(default, lazyProvider)
}

