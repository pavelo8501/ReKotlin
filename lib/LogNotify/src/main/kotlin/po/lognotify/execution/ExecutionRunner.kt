package po.lognotify.execution

import kotlinx.coroutines.delay
import po.lognotify.TasksManaged
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.common.result.PreliminaryResult
import po.misc.exceptions.ManagedException
import po.misc.functions.RepeatAttempts2
import po.misc.functions.RepeatResult
import po.misc.functions.models.RepeatAttempts
import po.misc.functions.repeatIfFaulty
import po.misc.functions.repeatIfFaultySuspending
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo

@PublishedApi
internal inline fun <T: TasksManaged, R: Any?> RunnableContainer<T, R>.repeatExecution(
    container: RunnableContainer<T, R>,
    crossinline block: (RepeatAttempts) -> R
): R = repeatIfFaulty(container.attempts,  block)

@PublishedApi
internal suspend inline fun <T: TasksManaged, R: Any?> ControlledExecution.repeatExecutionSuspending(
    container: RunnableContainer<T, R>,
    noinline actionOnFault: suspend (Throwable) -> Unit,
    crossinline block: (RepeatAttempts2) -> R
):  RepeatResult<R> = repeatIfFaultySuspending(container.attempts, actionOnFault, block)


@PublishedApi
internal fun <T: TasksManaged, R: Any?> RunnableContainer<T, R>.controlledRun(
    execLambda: ()-> R
):R {
    println("Subscribing ${this.hashCode()}")
    classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }
   val result = repeatExecution(
        this,
//        actionOnFault = {
//            println("Call ${this.hashCode()}")
//            registeredManaged = processExceptionCase(this ,it)
//            Thread.sleep(this.taskConfig.delayMs)
//        },
        block = { data ->
            if (data.failDetected && data.attemptsTotal > 1) {
                println("Attempt:${data.currentAttempt}; Left: ${data.attemptsLeft}")
            }
            try {
                execLambda()
            } catch (th: Throwable) {
               val managed = processExceptionCase(this, th)
                Thread.sleep(this.taskConfig.delayMs)
                val res = PreliminaryResult(classInfoProvider.resolve())
                if (res.resultAcceptsNulls) {
                    res.provideAcceptableResult(null)
                    res.getAcceptableResult() as R
                } else {
                    throw managed
                }
            }
        }
    )
   return finalizeRun(result)
}


internal suspend inline fun <T: TasksManaged, reified R: Any?> RunnableContainer<T, R>.controlledSuspendedRun(
    crossinline  execLambda: ()-> R
):R {
    classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }
    var registeredManaged: ManagedException? = null

    val repeatResult = repeatExecutionSuspending(
        this,
        actionOnFault = {
            registeredManaged = processExceptionCase(this ,it)
            delay(taskConfig.delayMs)
        },
        block = {data ->
            execLambda()
        }
    )
    return  if(repeatResult.resultResolved){
        return repeatResult.result!!
    }else{
        val res =  PreliminaryResult(classInfoProvider.resolve())
        if(res.resultAcceptsNulls) {
            res.provideAcceptableResult(null)
            res.getAcceptableResult()
        }else{
            throw registeredManaged?:throw ManagedException("No result")
        }
    }
}