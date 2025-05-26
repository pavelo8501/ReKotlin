package po.exposify.dto.enums

import po.misc.interfaces.ValueBased

enum class Cardinality(override val value : Int) : ValueBased{
    ONE_TO_ONE(1),
    ONE_TO_MANY(2),
}