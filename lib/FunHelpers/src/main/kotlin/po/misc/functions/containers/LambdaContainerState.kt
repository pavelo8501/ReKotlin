package po.misc.functions.containers

import po.misc.functions.models.ContainerMode
import po.misc.functions.models.LambdaState


interface LambdaContainerState<V: Any>{

    val identifiedAs: String
    val state: LambdaState
    val containerMode: ContainerMode
    val persistedValue : V?
}