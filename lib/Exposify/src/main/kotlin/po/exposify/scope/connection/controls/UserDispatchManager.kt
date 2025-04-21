package po.exposify.scope.connection.controls

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserDispatchManager {
    private val userLocks = mutableMapOf<String, Mutex>()
    suspend fun <T> enqueue(sessionId: String, block: suspend () -> T): T {
        val mutex = synchronized(userLocks) {
            userLocks.getOrPut(sessionId) { Mutex() }
        }

        return mutex.withLock {
            block()
        }
    }
}