package po.misc.time

data class ExecutionTimeStamp(val name: String, var id: String) {

    var startTime: Long = System.nanoTime()
    var endTime: Long = 0L

    var elapsed: Float = 0.0F
        private set

    fun stopTimer(id: String? = null): ExecutionTimeStamp {
        if(id != null){
            this.id = id
        }
        endTime = System.nanoTime()
        elapsed = (endTime - startTime) / 1_000_000f
        return this
    }

    fun startTimer() {
        startTime = System.nanoTime()
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