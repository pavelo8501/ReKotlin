package po.misc.time

fun <T:MeasuredContext> T.start():T{
    executionTimeStamp.startTimer()
    return this
}

fun <T:MeasuredContext> T.stop(): ExecutionTimeStamp{
   return executionTimeStamp.stopTimer()
}