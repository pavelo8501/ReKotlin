package po.misc.time

fun <T:MeasuredContext> T.startTimer():T{
    executionTimeStamp.startTimer()
    return this
}

fun <T:MeasuredContext> T.stopTimer(): ExecutionTimeStamp{
   return executionTimeStamp.stopTimer()
}