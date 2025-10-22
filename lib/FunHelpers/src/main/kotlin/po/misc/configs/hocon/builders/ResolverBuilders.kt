package po.misc.configs.hocon.builders

import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.HoconConfigurable
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver

class ResolverBuilder<T: EventHost, C: HoconResolvable<C>, V: Any>(
    override val receiver:T,
    override val resolver: HoconResolver<C>,
    override val hoconPrimitive:  HoconPrimitives<V>
): HoconConfigurable<T, C, V>