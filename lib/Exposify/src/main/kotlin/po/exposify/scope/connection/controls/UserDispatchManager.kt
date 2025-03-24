package po.exposify.scope.connection.controls

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserDispatchManager {
    private val userLocks = mutableMapOf<Long, Mutex>()
    suspend fun <T> enqueue(userId: Long, block: suspend () -> T): T {
        val mutex = synchronized(userLocks) {
            userLocks.getOrPut(userId) { Mutex() }
        }

        return mutex.withLock {
            block()
        }
    }
}