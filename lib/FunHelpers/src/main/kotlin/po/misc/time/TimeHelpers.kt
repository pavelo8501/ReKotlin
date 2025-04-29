package po.misc.time

fun MeasuredContext.start(){
    executionTimeStamp.startTimer()
}

fun MeasuredContext.stop(): ExecutionTimeStamp{
   return executionTimeStamp.stopTimer()
}