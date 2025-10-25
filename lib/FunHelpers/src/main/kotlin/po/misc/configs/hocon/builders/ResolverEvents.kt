package po.misc.configs.hocon.builders

import po.misc.callbacks.event.eventOf
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.functions.NoResult

class ResolverEvents<C: HoconResolvable<C>>(
    internal val resolver: HoconResolver<C>
){

    val onStart = resolver.eventOf<HoconResolver<C>, Unit>(NoResult)
    val onComplete = resolver.eventOf<HoconResolver<C>, Unit>(NoResult)



}