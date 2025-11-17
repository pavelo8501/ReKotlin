package po.misc.configs.hocon.builders

import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.HoconConfigurable
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.models.HoconString
import po.misc.types.token.TypeToken

class ResolverBuilder<T: EventHost, C: HoconResolvable<C>, V: Any>(
    override val receiver:T,
    override val resolver: HoconResolver<C>,
    override val hoconPrimitive:  HoconPrimitives<V>
): HoconConfigurable<T, C, V>

inline fun <reified T: HoconResolvable<T>> T.resolver():HoconResolver<T>{
    return HoconResolver(TypeToken.create<T>())
}

inline fun <T: EventHost, reified C: HoconResolvable<C>> C.resolver(
    receiver:T,
    noinline block: ResolverBuilder<T, C, String>.() -> Unit
):HoconResolver<C>{
    val resolver = resolver()
    val builderContainer = ResolverBuilder(receiver, resolver, HoconString.Companion)
    builderContainer.block()
    return  builderContainer.resolver
}
