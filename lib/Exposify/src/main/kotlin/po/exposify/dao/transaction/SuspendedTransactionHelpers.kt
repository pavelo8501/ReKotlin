package po.exposify.dao.transaction

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.exposify.common.classes.ExposifyDebugger
import po.misc.context.CTX
import kotlin.coroutines.CoroutineContext


private suspend fun <T : CTX, R> T.runWithNewSuspendedTransaction(
    logDataProcessor: ExposifyDebugger<*, *>,
    warnIfNoTransaction: Boolean,
    coroutineContext: CoroutineContext,
    block: suspend T.() -> R
): R = newSuspendedTransaction(coroutineContext) {

    if (warnIfNoTransaction) {
        logDataProcessor.warn("Transaction lost context. Restoring")
    }

    addLogger(logDataProcessor)
    block()
}

suspend fun <T : CTX, R> T.withSuspendedTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*, *>,
    warnIfNoTransaction: Boolean,
    block: suspend T.() -> R
): R = if (TransactionManager.currentOrNull()?.connection?.isClosed != false) {
    runWithNewSuspendedTransaction(logDataProcessor, warnIfNoTransaction, Dispatchers.IO, block)
} else {
    block()
}

suspend fun <T : CTX, R> T.withSuspendedTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*, *>,
    warnIfNoTransaction: Boolean,
    coroutineContext: CoroutineContext,
    block: suspend T.() -> R
): R = if (TransactionManager.currentOrNull()?.connection?.isClosed != false) {
    runWithNewSuspendedTransaction(logDataProcessor, warnIfNoTransaction, coroutineContext, block)
} else {
    block()
}

fun currentTransaction(): Transaction?{
   return TransactionManager.currentOrNull()
}

fun isTransactionReady(): Boolean {
    return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
}