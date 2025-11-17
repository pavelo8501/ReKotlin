package po.misc.configs.hocon.builders

import po.misc.callbacks.event.HostedEvent
import po.misc.callbacks.event.eventOf
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.functions.NoResult

class ResolverEvents<C: HoconResolvable<C>>(
    internal val resolver: HoconResolver<C>
){
    val onStart: HostedEvent<HoconResolver<C>, Unit, Unit> = resolver.eventOf(NoResult)
    val onComplete: HostedEvent<HoconResolver<C>, Unit, Unit> = resolver.eventOf(NoResult)
}