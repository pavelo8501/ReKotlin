package po.misc.reflection.objects

import java.time.LocalDateTime
import kotlin.reflect.KType

interface Composed {



    fun <V: Any> getDefaultForType(kType: KType): V {
        val result = when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            LocalDateTime::class -> {
                LocalDateTime.now()
            }
            else -> null
        }
        return  result as V
    }
}