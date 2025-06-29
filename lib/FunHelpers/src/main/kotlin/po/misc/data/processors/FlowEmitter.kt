package po.misc.data.processors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import po.misc.data.printable.PrintableBase


interface EmittableFlow<T: PrintableBase<T>>{

    fun subscribeToDataEmissions(scope: CoroutineScope, collector: suspend (T) -> Unit): Job
    fun stopBroadcast()
    fun emitData(data: T)

}

class FlowEmitter<T>(): EmittableFlow<T> where T: PrintableBase<T>{
    private val subscriberJobs = mutableListOf<Job>()
    private val notificationFlow = MutableSharedFlow<T>(
        replay = 10,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val notifications: SharedFlow<T> = notificationFlow.asSharedFlow()
    override fun subscribeToDataEmissions(scope: CoroutineScope, collector: suspend (T) -> Unit): Job {
        val job = scope.launch {
            notifications.collect(collector)
        }
        subscriberJobs += job
        return job
    }

    override fun stopBroadcast() {
        subscriberJobs.forEach { it.cancel() }
        subscriberJobs.clear()
    }

    override fun emitData(data: T) {
        CoroutineScope(Dispatchers.Default).launch {
            notificationFlow.emit(data)
        }
    }
}

