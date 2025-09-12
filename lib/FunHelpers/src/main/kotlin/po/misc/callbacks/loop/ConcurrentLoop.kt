package po.misc.callbacks.loop

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import kotlin.time.DurationUnit


interface ConcurrentLoop<REQUEST : Any, UPDATE : Any> {
    val config: LoopConfig

    fun startAsync()
    suspend fun startBlocking()
    fun stop()
}



abstract class ConcurrentLoopBase<I: Any, UPDATE: Any>(
    override val config: LoopConfig,
    val withHooks: (LoopHooks<I, UPDATE>.()->Unit)? = null
):ConcurrentLoop<I, UPDATE> {

    abstract val loopHooks: LoopHooks<I, UPDATE>

    val stopMessage: String = "Stopped loop since no request/response callback provided"
    val stats: LoopStats<UPDATE> = LoopStats()

    protected var listenerScope = CoroutineScope(SupervisorJob() + CoroutineName("connector_listener"))

    private var loopJob: Job? = null
    private var boostedPollingLeft = config.boostWindowSize
    private var active: Boolean = false


    init {
        withHooks?.invoke(loopHooks)
    }

    abstract suspend fun requestCall():I
    abstract fun modificationCall(input: I): Map<Any, UPDATE>
    abstract suspend fun responseCall(output:   Map.Entry<Any, UPDATE>)

    private fun calculateDelay(): Long{
        if( stats.inBoostMode){
            return  config.requestDelay.toLong(DurationUnit.SECONDS) / 2
        }
        return  config.requestDelay.toLong(DurationUnit.SECONDS)
    }

    private fun startBoost(){
        if(!stats.inBoostMode){
            stats.inBoostMode = true
            boostedPollingLeft = config.boostWindowSize
        }
    }

    private fun decreaseBoost(){
        boostedPollingLeft --
        if (boostedPollingLeft <= 0) {
            stats.inBoostMode = false
        }
    }

    override fun startAsync() {
        active = true
        loopJob = listenerScope.launch {
            runLoop()
        }
    }
    override suspend fun startBlocking() {
        active = true
        listenerScope.async {
            runLoop()
        }.await()
    }

    private suspend fun handlePreProcessed(preProcessed:  Map<Any, UPDATE>){
        preProcessed.forEach { item ->
            responseCall(item)
        }
        stats.totalProcessedCount += preProcessed.values.size
        stats.perLoopProcessedCount = preProcessed.values.size
        stats.lastUpdateProcessed = preProcessed.values.lastOrNull()
    }

    private  suspend fun runLoop() {
        active = true
        while (active) {
            try {
                val requestedData = requestCall()
                val modifiedData = modificationCall(requestedData)
                if (modifiedData.isNotEmpty()) {
                    handlePreProcessed(modifiedData)
                    startBoost()
                } else {
                    decreaseBoost()
                }
                stats.loopsCount ++
                loopHooks.triggerOnLoop(stats.copy())
                val delay = calculateDelay()
                delay(delay)
            } catch (th: Throwable) {
                if (th is ManagedException) {
                    val message =
                        "Error polling connector: ${th.throwableToText()}. $stopMessage".output(Colour.RED)
                }
                loopHooks.triggerOnError(th)
                active = false
            }
        }
    }

    override fun stop(){
        active = false
        loopJob?.cancel()
    }
}




