package po.test.misc.dsl.configurator

import org.junit.jupiter.api.Test
import po.misc.context.log_provider.LogProvider
import po.misc.context.log_provider.startProceduralLog
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.processor.createLogProcessor
import po.misc.dsl.configurator.ConfigPriority
import po.misc.dsl.configurator.data.ConfigurationTracker
import po.misc.dsl.configurator.dslConfig

class TestDSLConfiguratorLogging : TraceableContext{

    private class Configurable: TraceableContext {
        var modifications : Int = 0
        val appliedStrings = mutableListOf<String>()
    }
    private class WorkerClass: LogProvider {
        override val logProcessor = createLogProcessor()

        fun produce(count: Int, prefix: String): List<String>{
            val result = mutableListOf<String>()
            for (i in 1..count){
                val string = "${prefix}_$i"
                info("Producing", string)
                result.add(string)
            }
            return result
        }
    }

    private val config1 = "config_1"
    private val config2 = "config_2"

    @Test
    fun `Sequential execution with procedural log enabled`() {
        val worker = WorkerClass()
        val worker2 = WorkerClass()
        val configurator = dslConfig<Configurable> {
            onConfigurationStart{
                startProceduralLog(processName)
                worker.useLogHandler(this@dslConfig)
            }
            onConfiguration { tracker ->
                when(tracker.configStage){
                    ConfigurationTracker.ConfigurationStage.Start -> {
                        tracker.logStep(tracker.completeName)
                    }
                    ConfigurationTracker.ConfigurationStage.Complete -> {
                        tracker.completeStep(tracker.completeName)
                    }
                }
            }
            onConfigurationComplete {
                outputResult()
            }

            buildGroup(ConfigPriority.Top) {
                addConfigurator(config1) { param ->
                    worker.produce(5, "WorkerClass")
                }
                addConfigurator(config2){
                    worker2.produce(3, "worker 2")
                }
            }
        }
        val configurable = Configurable()
        val string1 = "String_1"
        configurator.applyConfig(configurable, string1)
    }


    @Test
    fun `Group selective execution with procedural log enabled`(){
        val worker = WorkerClass()
        val configurator = dslConfig<Configurable> {
            onConfigurationStart{
                startProceduralLog(processName)
                worker.useLogHandler(this@dslConfig)
            }
            onConfiguration { tracker ->
                when(tracker.configStage){
                    ConfigurationTracker.ConfigurationStage.Start -> {
                        tracker.logStep(tracker.completeName)
                    }
                    ConfigurationTracker.ConfigurationStage.Complete -> {
                        tracker.completeStep(tracker.completeName)
                    }
                }
            }
            onConfigurationComplete {
                outputResult()
            }
            buildGroup(ConfigPriority.Top) {
                addConfigurator(config1) { param ->
                    worker.produce(5, "WorkerClass")
                }
            }
        }
        val configurable = Configurable()
        val string1 = "String_1"
        configurator.applyConfig(configurable, string1, ConfigPriority.Top)
    }
}