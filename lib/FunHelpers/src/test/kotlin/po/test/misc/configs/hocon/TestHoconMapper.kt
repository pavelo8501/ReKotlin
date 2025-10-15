package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.configs.assets.AssetsKeyConfig
import po.misc.configs.hocon.mapTo
import po.misc.configs.hocon.mapToByKeys
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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