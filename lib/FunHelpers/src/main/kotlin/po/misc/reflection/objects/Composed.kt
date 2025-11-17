package po.misc.reflection.objects

import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import java.time.LocalDateTime

interface Composed {

    fun <T: Any> getDefaultForType(typeData: TypeToken<T>): T? {
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
        return  result?.safeCast(typeData.kClass)
    }

}