package po.misc.callbacks.manager

import po.misc.collections.StaticTypeKey


data class PayloadSignature(
    val typeKey: StaticTypeKey<*>,
    val withResult: Boolean
)