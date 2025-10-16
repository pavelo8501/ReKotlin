package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.configs.assets.AssetsKeyConfig
import po.misc.configs.hocon.HoconBoolean
import po.misc.configs.hocon.HoconInt
import po.misc.configs.hocon.HoconLong
import po.misc.configs.hocon.HoconNullable
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconString
import po.misc.configs.hocon.applyConfig
import po.misc.configs.hocon.createResolver
import po.misc.configs.hocon.hoconListProperty
import po.misc.configs.hocon.hoconNestedProperty
import po.misc.configs.hocon.hoconProperty
import po.misc.configs.hocon.hoconTransforming
import po.misc.configs.hocon.mapTo
import po.misc.configs.hocon.mapToByKeys
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TestHoconMapper {

   internal data class DefaultConfig(
       var name: String = "",
       var value: Int = 0,
       override var assetsPath: String = ""
   ): AssetsKeyConfig

    data class MisusedConfig(
        val name: String = "",
        var value: Int = 0,
    )

    internal class NestedConfig(): HoconResolvable<NestedConfig>{
        override val resolver =  createResolver()

        val requestTimeOut by hoconProperty(HoconLong)

        val socketTimeout by hoconTransforming(HoconLong){
            it.milliseconds
        }

        val number  by hoconProperty(HoconInt)
        val boolean : Boolean by hoconProperty(HoconBoolean)
    }

    internal class Config(): HoconResolvable<Config>{
        override val resolver =  createResolver()


        val categories by hoconListProperty<Config, String>()
        val nested: NestedConfig  by hoconNestedProperty(NestedConfig())
        val assetsPath : String by hoconProperty(HoconString)
        val optional : String by hoconProperty(HoconString, mandatory = false)
        val nullableParam : String?  by hoconProperty(HoconNullable, HoconString)
    }

    @Test
    fun `Hocon property delegate`(){

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
    fun  `Config keys can be safely mapped to data models properties`(){
        val config = ConfigFactory.load().getConfig("app")
        val configModel = DefaultConfig()
        assertDoesNotThrow {
            config.mapToByKeys(configModel)
        }

        assertEquals("Some name", configModel.name)
        assertEquals(10, configModel.value)
    }

    @Test
    fun  `Config keys mapping produce warning if write is impossible`(){
        val config = ConfigFactory.load().getConfig("app")
        val configModel = MisusedConfig()
        val output = captureOutput {
            assertDoesNotThrow {
                config.mapToByKeys(configModel)
            }
        }
        assertTrue {
            output.output.contains("Write operation impossible property")
        }
        assertEquals("", configModel.name)
        assertEquals(10, configModel.value)
    }

    @Test
    fun  `Data models properties can be safely mapped to config`(){
        val configData = DefaultConfig()
        val config = ConfigFactory.load()
        assertDoesNotThrow {
            config.getString("app.name")
        }
        val processed = assertDoesNotThrow {
            config.mapTo(configData) { propertyName ->
                "app.$propertyName"
            }
        }
        assertEquals("Some name", processed.name)
        assertEquals(10, processed.value)
    }
}