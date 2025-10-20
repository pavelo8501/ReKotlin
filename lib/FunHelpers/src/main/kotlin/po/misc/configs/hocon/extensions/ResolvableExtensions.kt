package po.misc.configs.hocon.extensions

import com.typesafe.config.Config
import po.misc.configs.hocon.HoconResolvable

fun <T: HoconResolvable<T>> T.applyConfig(factory:  Config):T{
    resolver.readConfig(this, factory)
    return this
}





