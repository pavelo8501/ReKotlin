package po.misc.functions.models

import po.misc.functions.hooks.Change


data class ContainerResult<T: Any>(
    override val oldValue: T?,
    override val  newValue : T
): Change<T?, T>

