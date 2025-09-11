package po.exposify.dao.transaction

import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.common.classes.ExposifyDebugger
import po.misc.context.CTX


fun <T: CTX, R>  T.withTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*, *>,
    warnIfNoTransaction: Boolean,
    block: T.() -> R
): R =
    if (TransactionManager.currentOrNull()?.connection?.isClosed != false) {
        if (warnIfNoTransaction) {
            logDataProcessor.warn("Transaction lost context. Restoring")
        }
        transaction{
            addLogger(logDataProcessor)
            block()
        }
    } else {
        block()
    }




fun <T: CTX, R>  T.withTransactionIfNone(
    logDataProcessor: ExposifyDebugger<*, *>,
    block: T.() -> R
): R = withTransactionIfNone(logDataProcessor, false, block)