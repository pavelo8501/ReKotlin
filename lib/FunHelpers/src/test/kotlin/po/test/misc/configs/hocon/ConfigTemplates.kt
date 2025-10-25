package po.test.misc.configs.hocon

import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.createResolver
import po.misc.configs.hocon.models.HoconBoolean
import po.misc.configs.hocon.models.HoconInt
import po.misc.configs.hocon.models.HoconString
import po.misc.configs.hocon.properties.hoconNested
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.configs.hocon.properties.listProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


internal class New() : HoconResolvable<New> {
    override val resolver = createResolver()
}

internal class NewData(val test: TestHoconMapper) : HoconResolvable<NewData> {
    override val resolver = createResolver()
}

internal class NestedConfig() : HoconResolvable<NestedConfig> {
    override val resolver = createResolver()

    val requestTimeOut: Long by hoconProperty()
    val socketTimeout : Duration by hoconProperty() { long: Long ->
        long.milliseconds
    }
    val number by hoconProperty(HoconInt)
    val boolean: Boolean by hoconProperty(HoconBoolean)
}

internal class Config() : HoconResolvable<Config> {
    override val resolver = createResolver()

    val categories: List<String>  by listProperty()
    val nested: NestedConfig by hoconNested(NestedConfig())
    val assetsPath: String by hoconProperty(HoconString)

    val optional: String by hoconProperty(HoconString)

    val nullableParam: String? by hoconProperty()
}