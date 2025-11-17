package po.misc.types.token

import kotlin.reflect.KClass


data class CompereContainer(
    val kClass: KClass<*>,
    val nullable: Boolean?
)
