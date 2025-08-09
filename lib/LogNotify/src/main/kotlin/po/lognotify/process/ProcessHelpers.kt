package po.lognotify.process

import kotlin.coroutines.coroutineContext


internal suspend fun processInScope(): Process<*>? = coroutineContext[Process]