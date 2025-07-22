package po.misc.functions.builders

import po.misc.functions.containers.Adapter
import po.misc.functions.containers.DSLProvider
import po.misc.functions.containers.Provider
import po.misc.types.getOrManaged


class ReactiveBuilder<V: Any, R: Any>(
    val  builderFn: ((V)->R)? = null
) {

    var provider: Adapter<V, R>?  = null

    init {
        builderFn?.let {
            provider = Adapter(it)
        }
    }

    fun initialize(builderFn: (V)->R){
        provider = Adapter(builderFn)
    }

    fun build(input:V):R{
        return  provider.getOrManaged("Not initialized").trigger(input)
    }
}


//abstract class ReactiveBuilderFBound<V: Any, R: Any>(
//    val  builderFn: ()->R
//) {
//    var provider: Provider<V, R> = Provider(builderFn)
//
//    fun updateConfig(builderFn: ()->R){
//        provider = Provider(builderFn)
//    }
//    fun build(input:V):R{
//        return provider.trigger(input)
//    }
//}


fun <V: Any, R: Any>  ReactiveBuilder<V, R>.reactiveBuild(input:V):R {
   return build(input)
}


fun <V: Any, R: Any> initializeBuilder(builderFn: (V)->R): ReactiveBuilder<V, R>{
 return   ReactiveBuilder(builderFn)
}
