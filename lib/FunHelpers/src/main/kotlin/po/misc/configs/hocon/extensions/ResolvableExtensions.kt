package po.misc.configs.hocon.extensions

import com.typesafe.config.Config
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.builders.ResolverEvents
import po.misc.data.output.output


@JvmName("applyConfigAttached")
fun <C: HoconResolvable<C>> C.applyConfig(factory:  Config): C {
    resolver.readConfig(factory)
    return this
}

fun <C: HoconResolvable<C>> applyConfig(
    config:C,
    factory: Config,
    eventBuilder: (ResolverEvents<C>.()-> Unit)? = null
): C {
    eventBuilder?.let {
        it.invoke(config.resolver.events)
        "config.resolver.events listeners size: ${config.resolver.events.onComplete.listeners.size}".output()
    }
    config.resolver.readConfig(factory)
    return config
}







