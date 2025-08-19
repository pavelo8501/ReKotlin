package po.misc.time


import java.time.Instant


class ExecutionTimeStamp(): TimeHelper {

    var name: String = ""
        private set
    var id: String? = ""
        private set

    constructor(name: String, id: String):this(){
        this.name = name
        this.id = id
    }

    var startTimeUtc: Instant? = null

    var startTime: Long = System.nanoTime()

    var endTimeUtc: Instant? = null
    var endTime: Long = 0L

    var elapsed: Float = 0.0F
        private set

    fun stopTimer(): ExecutionTimeStamp {
        endTimeUtc = nowTimeUtc()
        endTime = System.nanoTime()
        elapsed = (endTime - startTime) / 1_000_000f
        return this
    }

    fun startTimer():ExecutionTimeStamp {
        startTimeUtc = nowTimeUtc()
        startTime = System.nanoTime()
        return this
    }

    fun provideNameAndId(name: String, id: String){
        this.name = name
        this.id = id
    }


    private var onStartFn : ((ExecutionTimeStamp)-> Unit)? = null
    fun onStart(onStartFn : (ExecutionTimeStamp)-> Unit){
        this.onStartFn = onStartFn
    }

    private var onStopFn : ((ExecutionTimeStamp)-> Unit)? = null
    fun onStop(onStopFn : (ExecutionTimeStamp)-> Unit){
        this.onStopFn = onStopFn
    }
}