package po.test.misc.dsl

import org.junit.jupiter.api.Test
import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import po.misc.dsl.configurator.ConfigPriority
import po.misc.dsl.configurator.DSLConfigurator
import po.misc.dsl.configurator.DSLGroup
import po.misc.dsl.configurator.configurator
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestDSLConfigurator: TraceableContext {

    private class Configurable: TraceableContext{
        var modifications : Int = 0
    }

    @Test
    fun `DSLConfigurator usage`(){

        val config = DSLConfigurator<Configurable>(ConfigPriority.Top, ConfigPriority.Default)

        config.addConfigurator(ConfigPriority.Top){ modifications ++ }
        config.addConfigurator(ConfigPriority.Default){ modifications ++ }

        assertEquals(1, config.groupSize(ConfigPriority.Top))
        assertEquals(1, config.groupSize(ConfigPriority.Default))

        val configurable = Configurable()
        config.applyConfig(configurable)
        assertEquals(2, configurable.modifications)
    }

    @Test
    fun `DSLConfigurator initialization by groups`(){


        class TopGroup: HasNameValue{
            override val name: String = "Named"
            override val value: Int = 1
        }

        val topGroup = TopGroup()
        val namedGroup = DSLGroup<Configurable>(topGroup)
        val config = DSLConfigurator<Configurable>(namedGroup.asList())

        config.addConfigurator(ConfigPriority.Top){ modifications ++ }
        val configurable = Configurable()
        config.sequence {group->
            if(group.priority == topGroup){

                group.applyConfig(configurable)
            }
        }
        assertEquals(1, config.groupSize(ConfigPriority.Top))
        assertEquals(0, config.groupSize(ConfigPriority.Default))
        assertEquals(1, configurable.modifications)
    }

    @Test
    fun `DSLConfigurator initialization enum`(){
        val config = DSLConfigurator<Configurable, ConfigPriority>()
        assertEquals(2, config.prioritized.size)

    }

    @Test
    fun `DSLParametrized group`(){

        var receivedThroughConfig: Any? = null
        val config = DSLConfigurator<TestDSLConfigurator>()
        config.addParametrizedGroup<Int>(ConfigPriority.Top){
            addConfigurator {parameter->
                receivedThroughConfig = parameter
            }
        }
        config.applyConfig(this, 300)
        assertEquals(300, receivedThroughConfig)
    }

    @Test
    fun `DSLConfigurator builder`(){
        val config1Name = "config_1"
        val config2Name = "config_2"
        val config: DSLConfigurator<TestDSLConfigurator> = configurator(){
            addGroup(ConfigPriority.Top){
                addConfigurator(config1Name) {}
            }
            addGroup(ConfigPriority.Default){
                addConfigurator(config2Name) {}
            }
        }
        assertEquals(2, config.prioritized.size)
        assertEquals(2, config.size)
        assertNotNull(config.prioritized.values.firstOrNull()){
            assertEquals(ConfigPriority.Top, it.priority)
            assertNotNull(it.configurators.firstOrNull()){config->
                assertEquals(config1Name, config.lambdaName)
            }
        }
    }
}