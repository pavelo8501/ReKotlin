package po.misc.callbacks.loop

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import po.misc.data.helpers.output
import po.misc.data.logging.LogEmitter
import po.misc.data.logging.LogEmitterClass
import po.misc.data.styles.Colour
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import kotlin.time.Duration
import kotlin.time.DurationUnit


interface ConcurrentLoop<REQUEST : Any, UPDATE : Any> {
    val config: LoopConfig

    fun start(scope: CoroutineScope? = null)
    suspend fun startAndWait(scope: CoroutineScope? = null)
    fun stop()
}


interface ModifiedOutput {
    val size: Int
}


abstract class ConcurrentLoopBase<INPUT: Any, OUTPUT: ModifiedOutput>(
    override val config: LoopConfig,
    val withHooks: (LoopHooks<INPUT, OUTPUT>.()->Unit)? = null
):ConcurrentLoop<INPUT, OUTPUT> {

    abstract val loopHooks: LoopHooks<INPUT, OUTPUT>

    val stopMessage: String = "Stopped loop since no request/response callback provided"
    val stats: LoopStats = LoopStats()

    val emitter: LogEmitterClass = LogEmitterClass(this)

    protected var listenerScope = CoroutineScope(SupervisorJob() + CoroutineName("connector_listener"))

    private var loopJob: Job? = null

    private var boostedPollingLeft = config.boostWindowSize
    private var active: Boolean = false


    init {
        withHooks?.invoke(loopHooks)

    }

    abstract suspend fun requestCall():INPUT
    abstract fun modificationCall(input: INPUT): OUTPUT
    abstract suspend fun responseCall(output: OUTPUT)

    private fun calculateDelay(): Duration {
        return if (stats.inBoostMode) {
            config.requestDelay / 2
        } else {
            config.requestDelay
        }
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

    override fun start(scope: CoroutineScope?) {
        scope?.let { listenerScope = it }
        active = true
        loopJob = listenerScope.launch {
            runLoop()
        }
    }
    override suspend fun startAndWait(scope: CoroutineScope?) {
        scope?.let { listenerScope = it }
        active = true
        loopJob = listenerScope.launch {
            runLoop()
        }
        loopJob?.join()
    }

    private fun updateStats(preProcessed: OUTPUT){
        stats.totalProcessedCount += preProcessed.size
        stats.perLoopProcessedCount = preProcessed.size
    }

    private  suspend fun runLoop() {
        active = true
        while (active) {
            try {
                val delay = calculateDelay()
                emitter.info("Starting new loop #${stats.loopsCount} with delay of: $delay")
                val requestedData = requestCall()
                val modifiedData = modificationCall(requestedData)
                emitter.info("Modified data size: ${modifiedData.size}")
                if (modifiedData.size > 0) {
                    updateStats(modifiedData)
                    emitter.info("Emitting response")
                    responseCall(modifiedData)
                    startBoost()
                } else {
                    decreaseBoost()
                }
                emitter.info("Loop complete")
                stats.loopsCount++
                loopHooks.triggerOnLoop(stats.copy())
                delay(delay)
            }catch (th: Throwable){
                val message = "Error polling connector: ${th.throwableToText()}. $stopMessage".output(Colour.Red)
                "Error polling connector".output(Colour.Red)
                th.output()
                loopHooks.triggerOnError(th)
                active = false
                throw th
            }
        }
    }

    override fun stop(){
        active = false
        loopJob?.cancel()
    }
}




