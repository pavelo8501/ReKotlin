package po.misc.reflection.properties.models

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

data class MappingCheck(val component: Identifiable, val from: ValueBased, val to: ValueBased)