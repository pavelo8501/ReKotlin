package po.misc.callbacks

import po.misc.collections.StaticTypeKey


data class PayloadSignature(
    val typeKey: StaticTypeKey<*>,
    val withResult: Boolean
)