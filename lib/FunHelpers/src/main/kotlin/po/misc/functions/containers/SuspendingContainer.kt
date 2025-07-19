package po.misc.functions.containers

abstract class SuspendingContainer<I, R> {
    protected var valueBacking: I? = null
    abstract val result: R?
    abstract suspend fun trigger(): R?
    abstract suspend fun provideValue(value: I)
}

class SuspendingProvider<R : Any?>(
    val lambda: suspend () -> R
) : SuspendingContainer<Unit, R?>() {

    private var resultBacking: R? = null
    override val result: R? get() = resultBacking

    override suspend fun trigger(): R? {
        resultBacking = lambda()
        return resultBacking
    }

    override suspend fun provideValue(value: Unit) {
        // No-op
    }
}

class SuspendingEvaluator<V : Any>(
    val lambda: suspend (V) -> Boolean
) : SuspendingContainer<V, Boolean>() {

    private var resultBacking = false
    override val result: Boolean get() = resultBacking

    override suspend fun trigger(): Boolean {
        valueBacking?.let {
            resultBacking = lambda(it)
        } ?: run {
            resultBacking = false
        }
        return resultBacking
    }

    override suspend fun provideValue(value: V) {
        valueBacking = value
    }
}

class SuspendingAdapter<V : Any, R : Any?>(
    val lambda: suspend (V) -> R
) : SuspendingContainer<V, R?>() {

    private var resultBacking: R? = null
    override val result: R? get() = resultBacking

    override suspend fun trigger(): R? {
        valueBacking?.let {
            resultBacking = lambda(it)
        } ?: run {
            println("Value parameter not provided")
            null
        }
        return resultBacking
    }

    override suspend fun provideValue(value: V) {
        valueBacking = value
    }
}