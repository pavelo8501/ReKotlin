package po.test.misc.dsl.configurator

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.dsl.configurator.ConfigPriority
import po.misc.dsl.configurator.DSLParameterGroup
import po.misc.dsl.configurator.data.ConfigurationTracker
import po.misc.dsl.configurator.dslConfig
import po.misc.dsl.configurator.dslConfigForContext
import po.misc.dsl.configurator.dslConfigFromEnum
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDSLConfigurator: TraceableContext {

    private class Configurable: TraceableContext {
        var modifications : Int = 0
        val appliedStrings = mutableListOf<String>()
        var nullHits: Int = 0
        val configsProcessed = mutableListOf<String>()
        val progress = mutableListOf<Pair<ConfigurationTracker.ConfigurationStage, String>>()

    }

    private val config1 = "config_1"
    private val config2 = "config_2"

    @Test
    fun `DSLConfigurator build samples`(){

        val nullableParamConfig = "nullable parameter config"
        val configNoParameter = "parameterless config"

        val configurator = dslConfigFromEnum<Configurable, ConfigPriority>()

        val builderBySpecificClass = dslConfig<Configurable> {
            buildGroup(tokenOf<String>(), ConfigPriority.Top) {
                addConfigurator(config1) { stringParameter ->
                }
            }
            buildGroup(ConfigPriority.Default) {
                addConfigurator(configNoParameter) {
                }
            }
        }

        val contextualBuilder =  dslConfigForContext {
            buildGroup<String?>(DSLParameterGroup.Companion, ConfigPriority.Top){
                addConfigurator(nullableParamConfig){stringParameter->
                }
            }
            buildGroup(ConfigPriority.Default){
                addConfigurator(configNoParameter){
                }
            }
        }
        assertEquals(2, configurator.dslGroups.size)
        assertEquals(2, builderBySpecificClass.dslGroups.size)
        assertEquals(2, contextualBuilder.dslGroups.size)
        val dslGroup = contextualBuilder.dslGroups.entries.firstOrNull { it.value.priority == ConfigPriority.Top }?.value
        assertNotNull(dslGroup) { group ->
            assertTrue { group.parameterType.isNullable }
        }
    }

    @Test
    fun `DSLConfigurator handles nullability`(){
        val configurator = dslConfig<Configurable> {
            buildGroup(TypeToken.Companion.create<String?>(), ConfigPriority.Top) {
                addConfigurator(config1) { param ->
                    if (param != null) {
                        appliedStrings.add("${ConfigPriority.Top.name}_${config1}_$param")
                    }
                }
                addConfigurator { param ->
                    if (param != null) {
                        appliedStrings.add("${ConfigPriority.Top.name}_Anonymous_$param")
                    }
                }
            }
            buildGroup<String?>(DSLParameterGroup.Companion, ConfigPriority.Default) {
                addConfigurator(config1) { param ->
                    if (param != null) {
                        appliedStrings.add("${ConfigPriority.Default.name}_${config1}_$param")
                    } else {
                        nullHits++
                    }
                }
                addConfigurator(config2) { param ->
                    if (param != null) {
                        appliedStrings.add("${ConfigPriority.Default.name}_${config2}_$param")
                    } else {
                        nullHits++
                    }
                }
            }
        }
        val configurable = Configurable()
        val string1 = "String_1"
        val string2 : String?  = null

        configurator.applyConfig(configurable, string1, ConfigPriority.Top)
        configurator.applyConfig(configurable, string2, ConfigPriority.Default)
        assertEquals(2, configurable.appliedStrings.size)
        assertEquals(2, configurable.nullHits)
        val str = configurable.appliedStrings.take(2).joinToString(separator = " ") { it }
        assertTrue {
            str.contains(string1) &&
                    str.contains(ConfigPriority.Top.name) &&
                    str.contains("Anonymous")
        }
        configurable.appliedStrings.clear()
        configurable.nullHits = 0
        configurator.applyConfig(configurable, string1)
        assertEquals(4, configurable.appliedStrings.size)
        assertEquals(0, configurable.nullHits)
        val secondString = configurable.appliedStrings.take(2).joinToString(separator = " ") { it }
        val secondStringTail = configurable.appliedStrings.drop(2).take(2).joinToString(separator = " ") { it }
        assertEquals(str, secondString)
        assertTrue { secondStringTail.contains(string1) && secondStringTail.contains(ConfigPriority.Default.name) }
    }
}