package po.auth.interfaces

import kotlin.coroutines.CoroutineContext

/**
* Unified user session with coroutine scope and metadata storage.
*/
interface UserSession : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    val userId: Long
    val sessionId: String
    val createdAt: Long

    fun <T> setAttribute(key: String, value: T)
    fun <T> getAttribute(key: String): T?

    companion object Key : CoroutineContext.Key<UserSession>

}