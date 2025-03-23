package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap


class UserDispatchManager {

    internal class UserDispatchQueueManager{
        private val userJobs = ConcurrentHashMap<Long, Job>()

        suspend fun <T> enqueue(userId: Long, scope: CoroutineScope, block: suspend CoroutineScope.() -> T): T {
            val previousJob = userJobs[userId]

            val newJob = scope.launch(start = CoroutineStart.LAZY) {
                previousJob?.join()
                block()
            }

            userJobs[userId] = newJob
            newJob.start()
            newJob.join()

            return coroutineScope {
                @Suppress("UNCHECKED_CAST")
                (newJob as Deferred<T>).await()
            }
        }
    }

    internal val queManager: UserDispatchQueueManager = UserDispatchQueueManager()

    private val userLocks = mutableMapOf<Long, Mutex>()

    suspend fun <T> withUserLock(userId: Long, block: suspend () -> T): T {
        val mutex = synchronized(userLocks) {
            userLocks.getOrPut(userId) { Mutex() }
        }

        return mutex.withLock {
            block()
        }
    }
}