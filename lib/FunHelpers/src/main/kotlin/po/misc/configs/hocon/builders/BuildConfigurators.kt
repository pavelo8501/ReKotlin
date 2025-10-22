package po.misc.configs.hocon.builders

import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.event

import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.HoconConfigurable
import po.misc.configs.hocon.HoconResolvable
import po.misc.functions.NoResult
import kotlin.collections.set
import kotlin.reflect.KProperty


//inline fun <T: EventHost, C: HoconResolvable<C>, reified V: Any> HoconConfigurable<T, C, V>.onResult(
//    property: KProperty<V>,
//    noinline predicate: (T)-> Boolean
//):  HoconResolver<C> {
//    val event = receiver.event<T, V>(NoResult){
//        this.withValidation(predicate)
//        this
//    }
//    resolver.entryResolved[property] = event
//    return  resolver
//}




