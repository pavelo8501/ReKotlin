package po.misc.functions.containers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class FlowContainer<I, R> {
    protected val input: MutableSharedFlow<I> = MutableSharedFlow(extraBufferCapacity = 1)
    abstract val output: Flow<R>

    suspend fun emitData(value: I) {
        println("FlowContainer emitData ${value}")
        input.emit(value)
    }
}

class FlowAdapter<I : Any, R : Any?>(
    val lambda: suspend (I) -> R
) : FlowContainer<I, R>() {
    override val output: Flow<R> = input.map { lambda(it) }
}

@OptIn(ExperimentalCoroutinesApi::class)
infix fun <A, B> FlowContainer<A, B>.then(next: FlowContainer<B, *>): Flow<*> {
    return this.output.onEach { next.emitData(it) }.flatMapLatest { next.output }
}