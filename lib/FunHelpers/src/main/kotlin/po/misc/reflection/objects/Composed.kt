package po.misc.reflection.objects

import po.misc.types.TypeData
import po.misc.types.TypeRecord
import po.misc.types.safeCast
import java.time.LocalDateTime
import kotlin.reflect.KType

interface Composed {

    fun <V: Any> getDefaultForType(typeData: TypeData<V>): V? {
        val result = when (typeData.kType.classifier) {
            Int::class -> -1
            String::class -> "Default"
            Boolean::class -> false
            Long::class -> -1L
            LocalDateTime::class -> {
                LocalDateTime.now()
            }
            else -> null
        }
        return  result?.safeCast(typeData.clazz)
    }
}