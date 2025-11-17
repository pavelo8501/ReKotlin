package po.misc.dsl.configurator

import po.misc.collections.lambda_list.LambdaWrapper
import po.misc.context.tracable.TraceableContext


enum class State{
    Initialized,
    Complete,
    Configurator,
    Failed
}

class ProgressTracker<T: Any>(
    val receiver: T,
    val group: DSLConfigurable<*, *>,
    val state: State = State.Initialized,
    val configName: String = group.priority.name,
) {

    constructor(
        receiver: T,
        group: DSLConfigurable<*, *>,
        wrapper: LambdaWrapper<*, *>
    ):this(receiver, group, State.Configurator, wrapper.lambdaName)


}

