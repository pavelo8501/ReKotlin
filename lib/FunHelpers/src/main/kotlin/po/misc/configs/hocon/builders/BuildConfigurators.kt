package po.misc.configs.hocon.builders

import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.Validator
import po.misc.callbacks.events.eventOf
import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.HoconConfigurable
import po.misc.configs.hocon.HoconResolvable
import po.misc.functions.NoResult
import kotlin.collections.set
import kotlin.reflect.KProperty


inline fun <T: EventHost, C: HoconResolvable<C>, reified V: Any> HoconConfigurable<T, C, V>.onResult(
    property: KProperty<V>,
    builder: Validator<T, V, Unit>.()-> Unit
):  HoconResolver<C> {
    val event = receiver.eventOf<T, V>(NoResult)
    val validation = Validator(event)
    event.registerValidator(validation)
    validation.builder()
    resolver.entryResolved[property] = event
    return  resolver
}




