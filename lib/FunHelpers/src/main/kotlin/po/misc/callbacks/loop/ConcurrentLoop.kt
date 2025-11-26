package po.misc.callbacks.loop

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import po.misc.data.logging.LogEmitterClass
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.exceptions.throwableToText
import kotlin.time.Duration


interface ConcurrentLoop<REQUEST : Any, UPDATE : Any> {
    val config: LoopConfig


    suspend fun startSuspending(scope: CoroutineScope): Job
    fun startAsync(scope: CoroutineScope): Job

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

   // protected var listenerScope = CoroutineScope(SupervisorJob() + CoroutineName("connector_listener"))

    private var activeJob: Job? = null

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

    override fun startAsync(scope: CoroutineScope): Job {
        active = true
        val loopJob = scope.launch {
            runLoop()
        }
        activeJob = loopJob
        return loopJob
    }

    override suspend fun startSuspending(scope: CoroutineScope): Job {
        active = true
        val context = currentCoroutineContext()
        val loopJob = scope.launch {
            runLoop()
        }
        activeJob = loopJob
        loopJob.join()
        return loopJob
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
                if(th !is CancellationException){
                    "Error polling connector: ${th.throwableToText()}. $stopMessage".output(Colour.Red)
                    loopHooks.triggerOnError(th)
                    active = false
                    throw th
                }
            }
        }
    }

    override fun stop(){
        active = false
        activeJob?.cancel()
    }
}




