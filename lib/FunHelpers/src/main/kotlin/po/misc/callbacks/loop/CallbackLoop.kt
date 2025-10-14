package po.misc.callbacks.loop

import po.misc.types.getOrManaged


class LoopCallbacks<INPUT: Any, OUTPUT: ModifiedOutput>{

    private var requestCallbackBacking: (suspend ()->INPUT)? = null
    internal val requestCallback: suspend ()-> INPUT get() = requestCallbackBacking.getOrManaged(this)
    fun onRequest(callback: suspend ()->INPUT){
        requestCallbackBacking = callback
    }

    private var modificationCallbackBacking: ((INPUT)-> OUTPUT)? = null
    internal val modificationCallback: (INPUT)-> OUTPUT get() = modificationCallbackBacking.getOrManaged(this)

    fun onModification(callback:(INPUT)-> OUTPUT){
        modificationCallbackBacking = callback
    }

    private var responseCallbackBacking : (suspend (OUTPUT)-> Unit)? = null
    internal val responseCallback : suspend (OUTPUT)-> Unit get() = responseCallbackBacking.getOrManaged(this)
    fun onResponse(callback: suspend (OUTPUT)-> Unit){
        responseCallbackBacking = callback
    }

}


class CallbackLoop<INPUT: Any, OUTPUT: ModifiedOutput>(
    config: LoopConfig = LoopConfig(),
    hookBuilder: CallbackHooks<INPUT, OUTPUT>.()->Unit
):ConcurrentLoopBase<INPUT, OUTPUT>(config) {

    override val loopHooks: CallbackHooks<INPUT, OUTPUT> = CallbackHooks(this, LoopCallbacks())

    init {
        loopHooks.hookBuilder()
    }

    override suspend fun requestCall(): INPUT {
        return loopHooks.callbacks.requestCallback.invoke()
    }

    override fun modificationCall(input: INPUT): OUTPUT{
        return loopHooks.callbacks.modificationCallback.invoke(input)
    }

    override suspend fun responseCall(output: OUTPUT) {
        loopHooks.callbacks.responseCallback.invoke(output)
    }
}