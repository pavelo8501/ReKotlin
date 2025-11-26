package po.misc.dsl.configurator.data

import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.eventOf
import po.misc.collections.lambda_list.LambdaWrapper
import po.misc.context.log_provider.LogProvider
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.procedural.ProceduralHandler
import po.misc.dsl.configurator.DSLConfigurable
import po.misc.dsl.configurator.DSLConfigurator
import po.misc.dsl.configurator.data.ConfiguratorInfo.ConfiguratorStep
import po.misc.functions.NoResult




class ConfigurationTracker<T: TraceableContext>(
    val configurator: DSLConfigurator<T>,
    val receiver: T,
    val group: DSLConfigurable<*, *>,
    val wrapper: LambdaWrapper<*, *>
) : ProceduralHandler, EventHost {

    enum class ConfigurationStage{ Start, Complete }

    val groupName: String get() =  group.priority.name
    val stepName: String get() = wrapper.lambdaName
    val completeName : String get() =  "$groupName/$stepName"

    var configStage: ConfigurationStage = ConfigurationStage.Start
        internal set

    fun finalizeStep(): ConfigurationTracker<T>{
        configStage = ConfigurationStage.Complete
        return this
    }

    internal val configurationStep2 = eventOf<ConfigurationTracker<T>, ConfigurationStage>(NoResult)

    override val logProvider: LogProvider get() = configurator

}

