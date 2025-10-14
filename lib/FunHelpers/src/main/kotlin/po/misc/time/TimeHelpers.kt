package po.misc.time

fun <T:MeasuredContext> T.startTimer():ExecutionTimeStamp{
    return executionTimeStamp.startTimer()
}

fun <T:MeasuredContext> T.stopTimer(): ExecutionTimeStamp{
   return executionTimeStamp.stopTimer()
}