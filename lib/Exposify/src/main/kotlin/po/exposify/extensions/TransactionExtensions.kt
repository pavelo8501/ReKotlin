package po.exposify.extensions

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.common.classes.ExposifyDebugger
import po.misc.context.CTX
import kotlin.coroutines.CoroutineContext


private suspend fun <T : CTX, R> T.runWithNewTransaction(
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
    runWithNewTransaction(logDataProcessor, warnIfNoTransaction, Dispatchers.IO, block)
} else {
    block()
}

suspend fun <T : CTX, R> T.withSuspendedTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*, *>,
    warnIfNoTransaction: Boolean,
    coroutineContext: CoroutineContext,
    block: suspend T.() -> R
): R = if (TransactionManager.currentOrNull()?.connection?.isClosed != false) {
    runWithNewTransaction(logDataProcessor, warnIfNoTransaction, coroutineContext, block)
} else {
    block()
}

fun <T> withTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*,*>,
    warnIfNoTransaction: Boolean,
    block: () -> T): T {
    return if (TransactionManager.currentOrNull() == null || TransactionManager.current().connection.isClosed) {
        if(warnIfNoTransaction){
            logDataProcessor.warn("Transaction lost context. Restoring")
        }
        transaction {
            addLogger(logDataProcessor)
            block()
        }
    } else {
        block()
    }
}

fun <T, R> T.withTransactionRestored(
logDataProcessor: ExposifyDebugger<*, *>,
warnIfNoTransaction: Boolean = true,
block: T.() -> R
): R where T: CTX, R: Any? {

    val context = this@withTransactionRestored

    return if (TransactionManager.currentOrNull()?.connection?.isClosed != false) {
        if (warnIfNoTransaction) {
            logDataProcessor.warn("Transaction lost context. Restoring")
        }
        transaction {
            addLogger(logDataProcessor)
            block(context)
        }
    } else {
        block(context)
    }
}

fun currentTransaction(): Transaction?{
   return TransactionManager.currentOrNull()
}

fun isTransactionReady(): Boolean {
    return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
}