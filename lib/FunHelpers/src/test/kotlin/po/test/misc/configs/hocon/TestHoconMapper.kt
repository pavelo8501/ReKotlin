package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.callbacks.common.EventHost
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.createResolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.data.helpers.output
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TestHoconMapper: EventHost {


    private class TransformingConfig() : HoconResolvable<TransformingConfig> {
        override val resolver = createResolver()
        val socketTimeout by hoconProperty{ long: Long ->
            long.milliseconds
        }
    }

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
        val complete = applyConfig(Config(), factory){
            onComplete.onEvent {
                logProcessor.records.size.output()
                logProcessor.records.output()
            }
        }
        complete.assetsPath
    }



}