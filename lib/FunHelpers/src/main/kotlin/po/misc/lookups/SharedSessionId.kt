package po.misc.lookups

import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


data class SharedSessionId(val id: Long) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<SharedSessionId>{
        fun generateUUID(): Long{
           return UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
        }
    }
}
