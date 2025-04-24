package po.lognotify.classes.task.runner

import po.misc.exceptions.ManagedException


interface TaskRunnerHandler<R: Any?>{
    suspend fun onResult(onResultCallback: suspend (result:R, time:Float)->Unit)
    suspend fun onUnhandled(onUnhandledCallback: suspend (ex: ManagedException, time:Float)->Unit)
}

class  TaskRunnerCallbacks<R: Any?>(


):TaskRunnerHandler<R>{
    var onResultFn: ( suspend(result:R, time:Float) -> Unit)? = null
    override suspend fun onResult(onResultCallback: suspend (result:R, time:Float)->Unit){
        onResultFn = onResultCallback
    }

    var onUnhandledFn: (suspend (ex:ManagedException, time:Float) -> Unit)? = null
    override suspend fun onUnhandled(onUnhandledCallback: suspend (ex:ManagedException, time:Float)->Unit){
        onUnhandledFn = onUnhandledCallback
    }

}