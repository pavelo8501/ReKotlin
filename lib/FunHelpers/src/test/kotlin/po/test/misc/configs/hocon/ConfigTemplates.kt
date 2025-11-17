package po.test.misc.configs.hocon

import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.builders.resolver
import po.misc.configs.hocon.models.HoconBoolean
import po.misc.configs.hocon.models.HoconInt
import po.misc.configs.hocon.models.HoconLong
import po.misc.configs.hocon.models.HoconString
import po.misc.configs.hocon.properties.hoconNested
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.configs.hocon.properties.listProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


internal class New() : HoconResolvable<New> {
    override val resolver = resolver()
}

internal class NewData() : HoconResolvable<NewData> {
    override val resolver = resolver()
}

internal class NestedConfig() : HoconResolvable<NestedConfig> {
    override val resolver = resolver()

    val requestTimeOut: Long by hoconProperty()

    val socketTimeout : Duration by hoconProperty() { long: Long ->
        long.milliseconds
    }
    val number by hoconProperty(HoconInt)
    val boolean: Boolean by hoconProperty(HoconBoolean)
}

internal class HoconConfig() : HoconResolvable<HoconConfig> {
    override val resolver = resolver()

    val categories: List<String>  by listProperty()
    val nested: NestedConfig by hoconNested(NestedConfig())
    val assetsPath: String by hoconProperty(HoconString)

    val optional: String by hoconProperty(HoconString)

    val nullableParam: String? by hoconProperty()
}

abstract class HoconTestBase{

    protected class AllPropertyData : HoconResolvable<AllPropertyData>{
        override val resolver = resolver()
        val requestTimeOut: Long by hoconProperty()
        val socketTimeout : Duration by hoconProperty(HoconLong){long ->
            long.milliseconds
        }
        val number by hoconProperty(HoconInt)
        val boolean: Boolean by hoconProperty(HoconBoolean)
        val string: String by hoconProperty()
    }
}