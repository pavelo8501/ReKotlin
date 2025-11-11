package po.misc.collections.lambda_map

import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.styles.Colour


internal class LambdaMap<T: Any, R>(): AbstractMutableMap<TraceableContext, CallableWrapper<T, R>>() {

    private val mutableMapBacking = mutableMapOf<TraceableContext, CallableWrapper<T, R>>()
    var onKeyOverwritten: ((Any) -> Unit)? = null

    override val entries: MutableSet<MutableMap.MutableEntry<TraceableContext, CallableWrapper<T, R>>> get() = mutableMapBacking.entries

    override fun put(key: TraceableContext, value: CallableWrapper<T, R>): CallableWrapper<T, R>? {
        onKeyOverwritten?.let {callback->
            if(mutableMapBacking.containsKey(key)){
                callback.invoke(key)
            }
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