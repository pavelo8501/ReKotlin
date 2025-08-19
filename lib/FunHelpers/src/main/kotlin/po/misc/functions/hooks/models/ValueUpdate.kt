package po.misc.functions.hooks.models

import po.misc.functions.hooks.Change

data class ValueUpdate<V1: Any?, V2: Any>(
    override val oldValue:V1,
    override val newValue:V2,
): Change<V1, V2>
