package po.misc.callbacks.loop


interface LoopHooks<T1: Any, T2: ModifiedOutput>{
    fun onError(callback: (Throwable)-> Unit)
    fun onLoop(callback: ConcurrentLoopBase<T1, T2>.(LoopStats)-> Unit)

    fun triggerOnLoop(loopStats: LoopStats)
    fun triggerOnError(th: Throwable)

}

class BaseHooks<T1: Any, T2: ModifiedOutput>(
   internal val loop: ConcurrentLoopBase<T1, T2>
): LoopHooks<T1, T2> {

    internal var onErrorCallback : ((Throwable)-> Unit)? = null
    override fun onError(callback: (Throwable)-> Unit){
        onErrorCallback = callback
    }
    override fun triggerOnError(th: Throwable){
        onErrorCallback?.invoke(th)
    }

    internal var onLoopCallback : (ConcurrentLoopBase<T1, T2>.(LoopStats)-> Unit)? = null
    override fun onLoop(callback: ConcurrentLoopBase<T1, T2>.(LoopStats)-> Unit){
        onLoopCallback = callback
    }
    override fun triggerOnLoop(loopStats: LoopStats){
        onLoopCallback?.invoke(loop, loopStats)
    }
}

class CallbackHooks<T1: Any, T2: ModifiedOutput>(
    internal val loop: CallbackLoop<T1, T2>,
    val callbacks: LoopCallbacks<T1, T2>
): LoopHooks<T1, T2> {

    internal var onErrorCallback : ((Throwable)-> Unit)? = null
    override fun onError(callback: (Throwable)-> Unit){
        onErrorCallback = callback
    }

    internal var onLoopCallback : (ConcurrentLoopBase<T1, T2>.(LoopStats)-> Unit)? = null
    override fun onLoop(callback: ConcurrentLoopBase<T1, T2>.(LoopStats)-> Unit){
        onLoopCallback = callback
    }

    override fun triggerOnLoop(loopStats: LoopStats) {
        onLoopCallback?.invoke(loop, loopStats)
    }

    override fun triggerOnError(th: Throwable) {
        onErrorCallback?.invoke(th)
    }
}
