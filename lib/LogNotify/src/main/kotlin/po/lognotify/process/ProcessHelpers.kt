package po.lognotify.process

import kotlin.coroutines.coroutineContext


internal suspend fun processInScope(): LoggerProcessImplementation<*>? = coroutineContext[LoggerProcessImplementation]