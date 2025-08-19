package po.exposify.scope.lifecycle

import kotlinx.coroutines.CoroutineScope

interface LifecycleScope: CoroutineScope {
    val lifecycleManager: LifecycleManager

}

class LifecycleManager {
    private val cleanupHooks = mutableListOf<() -> Unit>()
    private val initHooks = mutableListOf<() -> Unit>()

    fun onInit(hook: () -> Unit) {
        initHooks += hook
    }

    fun onCleanup(hook: () -> Unit) {
        cleanupHooks += hook
    }

    fun triggerInit() {
        initHooks.forEach { it.invoke() }
    }

    fun triggerCleanup() {
        cleanupHooks.forEach { it.invoke() }
        cleanupHooks.clear()
    }
}

