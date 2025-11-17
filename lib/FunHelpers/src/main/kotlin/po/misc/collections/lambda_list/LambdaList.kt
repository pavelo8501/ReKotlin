package po.misc.collections.lambda_list


class LambdaList<H: Any, T>() : AbstractMutableList<LambdaWrapper<H, T>>() {
    private val backingList  = mutableListOf<LambdaWrapper<H, T>>()
    override val size: Int get() = backingList.size

    override fun add(index: Int, element: LambdaWrapper<H, T>): Unit{
        backingList.add(index, element)
    }
    override fun removeAt(index: Int): LambdaWrapper<H, T>{
       return  backingList.removeAt(index)
    }
    override fun set(index: Int, element: LambdaWrapper<H, T>): LambdaWrapper<H, T>{
       return backingList.set(index, element)
    }
    override fun get(index: Int): LambdaWrapper<H, T> {
       return backingList[index]
    }

}

//
//internal class LambdaMap<T: Any, R>(
//): AbstractMutableMap<TraceableContext, CallableWrapper<T, R>>() {
//
//    private val mutableMapBacking = mutableMapOf<TraceableContext, CallableWrapper<T, R>>()
//    var onKeyOverwritten: ((Any) -> Unit)? = null
//
//    override val entries: MutableSet<MutableMap.MutableEntry<TraceableContext, CallableWrapper<T, R>>> get() = mutableMapBacking.entries
//
//    override fun put(key: TraceableContext, value: CallableWrapper<T, R>): CallableWrapper<T, R>? {
//        onKeyOverwritten?.let {callback->
//            if(mutableMapBacking.containsKey(key)){
//                callback.invoke(key)
//            }
//        }
//        return mutableMapBacking.put(key, value)
//    }
//
//    fun <T: Any, R> getCallables(): List<CallableWrapper<T, R>>{
//        val filtered = mutableMapBacking.values.filterIsInstance<CallableWrapper<T, R>>()
//        if(filtered.size !=  mutableMapBacking.values.size){
//            "getCallables returned less values than it should".output(Colour.Yellow)
//        }
//        return filtered
//    }
//}