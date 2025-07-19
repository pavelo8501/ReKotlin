package po.misc.functions.hooks

import po.misc.functions.containers.Producer


class DataNotifier<T: Any, V: Any>(
    var receiver: T?
): ResponsiveData<T, V> {

    private var beforeInitLambda: Producer<T>? = null
    private var initializedLambda: Producer<T>? = null
    private var disposedLambda: Producer<T>? = null

    private var valueUpdatedLambda: Producer<Change<V?, V>>? = null

    init {
        receiver?.let {receiver->
            beforeInitLambda?.provideValue(receiver)
        }
    }

    fun initialize(receiver: T){
        this.receiver = receiver
        triggerInitialized()
    }

    override fun triggerBeforeInitialized() {
        receiver?.let { receiver ->
            beforeInitLambda?.provideValue(receiver)
        }
        beforeInitLambda?.trigger()

    }
    override fun triggerInitialized() {

        receiver?.let { receiver ->
            initializedLambda?.provideValue(receiver)
        }
        initializedLambda?.trigger()
    }

    override fun triggerChanged(change: Change<V?, V>) {
        valueUpdatedLambda?.trigger(change)
    }

    override fun triggerDisposed() {
        receiver?.let { receiver ->
            disposedLambda?.provideValue(receiver)
        }
        disposedLambda?.trigger()
    }

    override fun onBeforeInitialized(onBeforeInitialized: (T) -> Unit) {
        beforeInitLambda = Producer(onBeforeInitialized)
    }

    override fun onInitialized(onInitialized: (T) -> Unit) {
        initializedLambda = Producer(onInitialized)
    }

    override fun onChanged(onChanged: (Change<V?, V>) -> Unit) {
        valueUpdatedLambda = Producer(onChanged)
    }

    override fun onDisposed(onDisposed: (T) -> Unit) {
        disposedLambda = Producer(onDisposed)
    }

}