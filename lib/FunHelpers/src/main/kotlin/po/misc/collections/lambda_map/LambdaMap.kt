package po.misc.collections.lambda_map

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.debugging.stack_tracer.StackResolver
import po.misc.debugging.stack_tracer.TraceResolver

class LambdaMap<T, T1, R>(
    val host: Component,
    val resolver: StackResolver = TraceResolver(host)
): StackResolver by resolver {

    internal val lambdaBacking = mutableMapOf<TraceableContext, LambdaWrapper<T, T1, R>>()
    internal val suspendedBacking = mutableMapOf<TraceableContext, SuspendedWrapper<T, T1, R>>()

    val lambdaMap: Map<TraceableContext, LambdaWrapper<T, T1, R>> get() = lambdaBacking
    val suspendedMap: Map<TraceableContext, SuspendedWrapper<T, T1, R>> get() = suspendedBacking
    val listeners : List<CallableWrapper<T, T1, R>> get() = lambdaBacking.values + suspendedBacking.values
    val listenerEntries : List<Map.Entry<TraceableContext,  CallableWrapper<T, T1, R>>> get(){
        return buildList {
            addAll(lambdaBacking.entries)
            addAll(suspendedBacking.entries)
        }
    }


    var onKeyOverwritten: ((Any) -> Unit)? = null

    val size: Int get() =  lambdaBacking.size + suspendedBacking.size

    private fun putLambda(key: TraceableContext, value: LambdaWrapper<T, T1, R>):LambdaWrapper<T, T1, R>?{
        val hasKey = lambdaBacking.containsKey(key)
        if(hasKey){
            resolver.resolveTrace("put")
        }
        return lambdaBacking.put(key, value)
    }
    private fun putSuspended(key: TraceableContext, value: SuspendedWrapper<T, T1, R>):SuspendedWrapper<T, T1, R>?{
        val hasKey = suspendedBacking.containsKey(key)
        if(hasKey){
            resolver.resolveTrace("put")
        }
        return suspendedBacking.put(key, value)
    }

    operator fun get(key: TraceableContext): CallableWrapper<T, T1, R>? =
        lambdaBacking[key] ?: suspendedBacking[key]
    operator fun set(key: TraceableContext, value: CallableWrapper<T, T1, R>) {
        when (value) {
            is LambdaWrapper -> putLambda(key, value)
            is SuspendedWrapper -> putSuspended(key, value)
        }
    }

    fun putIfAbsent(key: TraceableContext, value: CallableWrapper<T, T1, R>):CallableWrapper<T, T1, R>?{
       return when (value) {
            is LambdaWrapper ->  lambdaBacking.putIfAbsent(key, value)
            is SuspendedWrapper -> suspendedBacking.putIfAbsent(key, value)
        }
    }

    fun removeSuspended(key: TraceableContext):SuspendedWrapper<T, T1, R>?{
        return suspendedBacking.remove(key)
    }
    fun removeLambda(key: TraceableContext): LambdaWrapper<T, T1, R>?{
        return lambdaBacking.remove(key)
    }
    fun remove(key: TraceableContext): CallableWrapper<T, T1, R>?{
        val lambdaRemoved = removeLambda(key)
        if(lambdaRemoved != null){
            return lambdaRemoved
        }
        return  removeSuspended(key)
    }

    fun getLambda(key: TraceableContext): LambdaWrapper<T, T1, R>?{
        return lambdaBacking[key]
    }
    fun getSuspended(key: TraceableContext): SuspendedWrapper<T, T1, R>?{
        return suspendedBacking[key]
    }

    fun clearSuspended(){
        suspendedBacking.clear()
    }

    fun clearLambdas(){
        lambdaBacking.clear()
    }

    fun clear(){
        clearLambdas()
        clearSuspended()
    }

    fun <T: Any, R> getCallables(): List<CallableWrapper<T, T1, R>>{
        val lambdas = lambdaBacking.values.filterIsInstance<CallableWrapper<T, T1, R>>()
        val suspended = suspendedBacking.values.filterIsInstance<CallableWrapper<T, T1, R>>()
        val filtered = buildList {
            addAll(lambdas)
            addAll(suspended)
        }
        if(filtered.size !=  lambdaBacking.size + suspendedBacking.size){
            "getCallables returned less values than it should".output(Colour.Yellow)
        }
        return filtered
    }
}