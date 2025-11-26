package po.misc.dsl.configurator.data

import po.misc.context.log_provider.LogProvider
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.log_subject.StartProcessSubject
import po.misc.data.logging.log_subject.startProcSubject
import po.misc.data.logging.log_subject.updateText
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralHandler
import po.misc.dsl.configurator.DSLConfigurator
import po.misc.functions.Throwing

class ConfiguratorInfo<T: TraceableContext>(
    internal val configurator: DSLConfigurator<T>,
    val configGroupsTotal: Int,
    val configsTotal: Int,
) : ProceduralHandler {
    enum class ConfiguratorStep{ Start, Complete  }

    private var processed: Int = 0

    var step: ConfiguratorStep = ConfiguratorStep.Start
        internal set

    private val processDescription = "Configuring Groups / $configGroupsTotal Configs / $configsTotal"

    val processName: StartProcessSubject get() {
       return configurator.startProcSubject("Start configuration").updateText(processDescription)
    }

    override val logProvider: LogProvider get() = configurator

    internal fun finalizeResult(groupsProcessed: Int): ConfiguratorInfo<T>{
        step = ConfiguratorStep.Complete
        processed = groupsProcessed
        return this
    }

    fun outputResult(){
        val processor = logProvider.logProcessor
        val handler = processor.getHandlerOf<ProceduralFlow<*>>(Throwing)
        processor.finalizeFlow(handler)
    }
}