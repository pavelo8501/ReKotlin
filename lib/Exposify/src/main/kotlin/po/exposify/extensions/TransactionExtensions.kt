package po.exposify.extensions

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.common.classes.ExposedDebugger
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.task.TaskHandler
import kotlin.coroutines.CoroutineContext


suspend fun <T> withSuspendedTransactionIfNone(
    logDataProcessor: ExposedDebugger<*,*>,
    warnIfNoTransaction: Boolean,
    block: suspend () -> T): T {
    return if (TransactionManager.currentOrNull() == null || TransactionManager.current().connection.isClosed) {
       if(warnIfNoTransaction){ logDataProcessor.warn("Transaction lost context. Restoring") }
        newSuspendedTransaction(Dispatchers.IO) {
            addLogger(logDataProcessor)
            block()
        }
    } else {
        block()
    }
}

fun <T> withTransactionIfNone(
    logDataProcessor: ExposedDebugger<*,*>,
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


fun isTransactionReady(): Boolean {
    return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
}