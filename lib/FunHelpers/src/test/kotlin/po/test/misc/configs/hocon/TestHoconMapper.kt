package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.models.HoconBoolean
import po.misc.configs.hocon.models.HoconInt
import po.misc.configs.hocon.models.HoconLong
import po.misc.configs.hocon.models.HoconNullable
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconString
import po.misc.configs.hocon.createResolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.properties.hoconList
import po.misc.configs.hocon.properties.hoconNested
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.configs.hocon.properties.propertyTransforming
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class TestHoconMapper: EventHost {

    internal class New() : HoconResolvable<New> {
        override val resolver = createResolver()
    }

    internal class NestedConfig() : HoconResolvable<NestedConfig> {
        override val resolver = createResolver()

        val requestTimeOut by hoconProperty(HoconLong)
        val socketTimeout by propertyTransforming(HoconLong) { long ->
            long.milliseconds
        }
        val number by hoconProperty(HoconInt)
        val boolean: Boolean by hoconProperty(HoconBoolean)
    }

    internal class Config() : HoconResolvable<Config> {
        override val resolver = createResolver()

        val categories by hoconList<Config, String>()
        val nested: NestedConfig by hoconNested(NestedConfig())
        val assetsPath: String by hoconProperty(HoconString)
        val optional: String by hoconProperty(HoconString, mandatory = false)
        val nullableParam: String? by hoconProperty(HoconNullable, HoconString)
    }

    internal class NewData(val test: TestHoconMapper) : HoconResolvable<NewData> {
        override val resolver = createResolver()
    }

//
//    internal class NewData(val test: TestHoconMapper) : HoconResolvable<NewData> {
////
////        override val resolver = createResolver(test) {
////
////
////
////            onResult(::parameter1) {
////                registerValidator { paramStr ->
////                    paramStr == "something"
////                }
////                onValidationSuccess {
////                    "Print that we are the successors of success".output(Colour.GreenBright)
////                }
////                onValidationFailure {
////                    "Better luck next time".output(Colour.YellowBright)
////                }
////            }
////        }
//        val parameter1: String by hoconProperty(HoconString)
//        val intParam1: Int by hoconProperty(HoconInt, mandatory = false)
//    }

    @Test
    fun `Hocon property delegate`() {

        val factory = ConfigFactory.load().getConfig("app")
        val config = Config()
        config.applyConfig(factory)

        assertEquals("/var/data/assets", config.assetsPath)
        assertNull(config.nullableParam)
        assertEquals("Optional value", config.optional)
        assertEquals(42, config.nested.number)
        assertEquals(true, config.nested.boolean)
        assertEquals(3, config.categories.size)
    }

    @Test
    fun `Hocon createResolver validatable config builder`() {
        val factory = ConfigFactory.load().getConfig("app2")
        val newData = NewData(this)
        newData.applyConfig(factory)
    }

}