package po.misc.callbacks.delayed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import po.misc.callbacks.common.EventHost
import po.misc.types.token.TypeToken


enum class DelayedStatus{
    Armed,
    InAction,
    Complete
}

class DelayedEvent<H, T>(
    val host: H,
    paramType: TypeToken<T>,
    private val delayedEventScope: CoroutineScope  = CoroutineScope(Dispatchers.IO)
) where H: EventHost, T: Any {

    private var status: DelayedStatus = DelayedStatus.Armed
    private var finalizationCallValue:T? = null
    private var timerJob: Job? = null

    private var mainEventCallback: (H.(T)-> Unit)? = null
    private var mainEventSuspendedCallback: (suspend  H.(T)-> Unit)? = null

    private fun preStartRoutine(){
        status = DelayedStatus.InAction
        finalizationCallValue = null
    }

    private suspend fun triggerBoth(value: T){
        mainEventCallback?.invoke(host, value)
        mainEventSuspendedCallback?.invoke(host, value)
    }

    private fun crateTickPayload(currentTick: Int, config: DelayWithTicks<*, *>, value :T): TickPayload<T>{

       return  TickPayload(tick = currentTick, config.ticksCount,config.tickTime, value)
    }

    fun onTimer(callback: H.(T)-> Unit){
        mainEventCallback = callback
    }
    fun onTimerSuspending(callback: suspend H.(T)-> Unit){
        mainEventSuspendedCallback = callback
    }

    suspend fun startTimerSuspending(config: DelayConfig, value :T) {
        preStartRoutine()
        delayedEventScope.async {
            if(status == DelayedStatus.InAction) {
                delay(config.fireAfter)
            }
            if(status == DelayedStatus.Complete) {
                finalizationCallValue?.let {
                    triggerBoth(it)
                }
            }else{
                triggerBoth(value)
            }
            finalizeCounter()
        }.await()
    }

    fun startTimer(config: DelayConfig, value :T) {
        preStartRoutine()
        delayedEventScope.launch {

            delay(config.fireAfter)
            triggerBoth(value)

            if (status == DelayedStatus.Complete) {
                delay(config.fireAfter)
                finalizationCallValue?.let {
                    triggerBoth(it)
                }
            }
            if (status == DelayedStatus.Armed || status == DelayedStatus.InAction) {
                triggerBoth(value)
            }
            finalizeCounter()
        }
    }

    fun startTimer(config: DelayWithTicks<H,T>, value:T){
        preStartRoutine()
        timerJob =  delayedEventScope.launch {
            repeat(config.ticksCount) {
                if (status == DelayedStatus.InAction){
                    delay(config.tickTime)
                    config.tickAction.invoke(host, crateTickPayload(it + 1, config, value))
                }
            }
            if (config.remainder > 0) {
                if (status == DelayedStatus.InAction){
                    delay(config.remainder)
                }
            }
            if(status == DelayedStatus.Complete){
                finalizationCallValue?.let {
                    triggerBoth(it)
                }
            }else{
                triggerBoth(value)
            }
            finalizeCounter()
        }
    }

    fun finalizeCounter(){
        status = DelayedStatus.Complete
        timerJob?.cancel()
    }

    fun finalizeCounter(value: T){
        status = DelayedStatus.Complete
        finalizationCallValue = value
        timerJob?.cancel()
    }

}