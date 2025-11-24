import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import po.test.misc.configs.hocon.HoconConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.builders.resolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.data.output.output
import po.test.misc.configs.hocon.NewData
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class TestHoconMapper: EventHost {


    private class TransformingConfig() : HoconResolvable<TransformingConfig> {
        override val resolver = resolver()
        val socketTimeout by hoconProperty{ long: Long ->
            long.milliseconds
        }
    }

    @Test
    fun `Hocon property delegate`() {
        val factory = ConfigFactory.load().getConfig("app")
        val config = HoconConfig()
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
        val newData = NewData()
        newData.applyConfig(factory)
    }

    @Test
    fun `Property with transformation work as expected`() {

        val factory = ConfigFactory.load().getConfig("test_transforming")
        val transformingConfig = TransformingConfig()
        transformingConfig.applyConfig(factory)
        val timeout = assertDoesNotThrow {
            transformingConfig.socketTimeout
        }
        assertEquals(timeout, transformingConfig.socketTimeout)
    }

    @Test
    fun `Hocon reporting`() {
        val factory = ConfigFactory.load().getConfig("app")
        val complete = applyConfig(HoconConfig(), factory){
            onComplete.onEvent {
                logProcessor.logRecords.size.output()
                logProcessor.logRecords.output()
            }
        }
        complete.assetsPath
    }



}