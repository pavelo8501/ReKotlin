package po.misc.collections.lambda_map

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.debugging.stack_tracer.StackResolver
import po.misc.debugging.stack_tracer.TraceResolver

class LambdaMap<T: Any, R>(
    val host: Component,
    val resolver: StackResolver = TraceResolver(host)
): AbstractMutableMap<TraceableContext, CallableWrapper<T, R>>(), StackResolver by resolver {

    private val mutableMapBacking = mutableMapOf<TraceableContext, CallableWrapper<T, R>>()

    var onKeyOverwritten: ((Any) -> Unit)? = null

    override val entries: MutableSet<MutableMap.MutableEntry<TraceableContext, CallableWrapper<T, R>>> get() = mutableMapBacking.entries

    override fun put(key: TraceableContext, value: CallableWrapper<T, R>): CallableWrapper<T, R>? {
        val hasKey = mutableMapBacking.containsKey(key)
        if(hasKey){
            resolver.resolveTrace("put")
        }
        return mutableMapBacking.put(key, value)
    }

    fun <T: Any, R> getCallables(): List<CallableWrapper<T, R>>{
        val filtered = mutableMapBacking.values.filterIsInstance<CallableWrapper<T, R>>()
        if(filtered.size !=  mutableMapBacking.values.size){
            "getCallables returned less values than it should".output(Colour.Yellow)
        }
        return filtered
    }
}