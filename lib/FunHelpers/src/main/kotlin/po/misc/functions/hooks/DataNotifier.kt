package po.misc.functions.hooks

import po.misc.functions.containers.Notifier


class DataNotifier<T: Any, V: Any>(
    var receiver: T?
): ResponsiveData<T, V> {

    private var beforeInitLambda: Notifier<T>? = null
    private var initializedLambda: Notifier<T>? = null
    private var disposedLambda: Notifier<T>? = null

    private var valueUpdatedLambda: Notifier<Change<V?, V>>? = null

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
       // beforeInitLambda?.trigger(receiver)

    }
    override fun triggerInitialized() {

        receiver?.let { receiver ->
            initializedLambda?.provideValue(receiver)
        }
       // initializedLambda?.trigger()
    }

    override fun triggerChanged(change: Change<V?, V>) {
        valueUpdatedLambda?.trigger(change)
    }

    override fun triggerDisposed() {
        receiver?.let { receiver ->
            disposedLambda?.provideValue(receiver)
        }
        //disposedLambda?.trigger()
    }

    override fun onBeforeInitialized(onBeforeInitialized: (T) -> Unit) {
        beforeInitLambda = Notifier(onBeforeInitialized)
    }

    override fun onInitialized(onInitialized: (T) -> Unit) {
        initializedLambda = Notifier(onInitialized)
    }

    override fun onChanged(onChanged: (Change<V?, V>) -> Unit) {
        valueUpdatedLambda = Notifier(onChanged)
    }

    override fun onDisposed(onDisposed: (T) -> Unit) {
        disposedLambda = Notifier(onDisposed)
    }

}