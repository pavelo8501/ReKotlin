package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import po.misc.configs.hocon.extensions.applyConfig
class TestHoconReporting: HoconTestBase() {

    @Test
    fun `Start process`(){

        val factory = ConfigFactory.load().getConfig("all_properties")
        val allProperties = AllPropertyData()
        allProperties.applyConfig(factory)
    }

}