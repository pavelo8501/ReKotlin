package po.misc.functions.hooks

import po.misc.functions.containers.Notifier


abstract class HooksBase<V: Any> {

    protected var notifier: Notifier<V>? = null
    fun subscribe(callback: (V) -> Unit) {
        notifier = Notifier(callback)
    }
    fun unsubscribe(){
        notifier = null
    }
}

class ChangeHook<V: Any>():HooksBase<Change<V?, V>>() {
    fun trigger(change: Change<V?, V>) {
        notifier?.trigger(change)
    }
}


class ErrorHook<V: Any>():HooksBase<ErrorPayload<V>>() {
    fun trigger(error: ErrorPayload<V>){
        notifier?.trigger(error)
    }
}

