package po.exposify.extensions

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.lognotify.classes.task.TaskHandler
import kotlin.coroutines.CoroutineContext

suspend fun <T> withTransactionIfNone(
    db: Database,
    context: CoroutineContext = Dispatchers.IO,
    block: suspend () -> T
): T {
    return if (TransactionManager.currentOrNull() == null || TransactionManager.current().connection.isClosed) {
        newSuspendedTransaction(context, db) { block() }
    } else {
        block()
    }
}


suspend fun <T> withTransactionIfNone(block: suspend () -> T): T {
    return if (TransactionManager.currentOrNull() == null || TransactionManager.current().connection.isClosed) {
        newSuspendedTransaction { block() }
    } else {
        block()
    }
}


suspend fun <T> withTransactionIfNone(taskHandler : TaskHandler<*>, block: suspend () -> T): T {
    return if (TransactionManager.currentOrNull() == null || TransactionManager.current().connection.isClosed) {
        taskHandler.warn("Transaction lost context. Restoring")
        newSuspendedTransaction { block() }
    } else {
        block()
    }
}


fun isTransactionReady(): Boolean {
    return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
}