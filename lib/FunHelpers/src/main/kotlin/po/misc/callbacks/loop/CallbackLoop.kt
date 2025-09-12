package po.misc.callbacks.loop

import po.misc.types.getOrManaged


class LoopCallbacks<INPUT: Any, OUTPUT: Any>{

    private var requestCallbackBacking: (suspend (OUTPUT?)->INPUT)? = null
    internal val requestCallback: suspend (OUTPUT?)-> INPUT get() = requestCallbackBacking.getOrManaged(this)
    fun onRequest(callback: suspend (OUTPUT?)->INPUT){
        requestCallbackBacking = callback
    }

    private var modificationCallbackBacking: ((INPUT)-> Map<Any, OUTPUT>)? = null
    internal val modificationCallback: (INPUT)-> Map<Any, OUTPUT> get() = modificationCallbackBacking.getOrManaged(this)

    fun onModification(callback:(INPUT)-> Map<Any, OUTPUT>){
        modificationCallbackBacking = callback
    }

    private var responseCallbackBacking : (suspend (Map.Entry<Any, OUTPUT>)-> Unit)? = null
    internal val responseCallback : suspend (Map.Entry<Any, OUTPUT>)-> Unit get() = responseCallbackBacking.getOrManaged(this)
    fun onResponse(callback: suspend (Map.Entry<Any, OUTPUT>)-> Unit){
        responseCallbackBacking = callback
    }

}


class CallbackLoop<REQUEST: Any, UPDATE: Any>(
    config: LoopConfig = LoopConfig(),
    hookBuilder: CallbackHooks<REQUEST, UPDATE>.()->Unit
):ConcurrentLoopBase<REQUEST, UPDATE>(config) {

    override val loopHooks: CallbackHooks<REQUEST, UPDATE> = CallbackHooks(this, LoopCallbacks())

    init {
        loopHooks.hookBuilder()
    }

    override suspend fun requestCall(): REQUEST {
        return loopHooks.callbacks.requestCallback.invoke(null)
    }

    override fun modificationCall(input: REQUEST): Map<Any, UPDATE> {
        return loopHooks.callbacks.modificationCallback.invoke(input)
    }

    override suspend fun responseCall(output: Map.Entry<Any, UPDATE>) {
        loopHooks.callbacks.responseCallback.invoke(output)
    }
}