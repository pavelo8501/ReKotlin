package po.exposify.dto.enums

import po.misc.data.NameValue

enum class Cardinality(override val value : Int) : NameValue{
    ONE_TO_ONE(1),
    ONE_TO_MANY(2),
}