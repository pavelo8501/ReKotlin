package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.data.helpers.output
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestHoconMapper: EventHost {


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

    @Test
    fun `Hocon reporting`() {
        val factory = ConfigFactory.load().getConfig("app")
        val complete = applyConfig(Config(), factory){
            onComplete.onEvent {
                logProcessor.records.size.output()
                logProcessor.records.output()
            }
        }
        complete.assetsPath


    }



}