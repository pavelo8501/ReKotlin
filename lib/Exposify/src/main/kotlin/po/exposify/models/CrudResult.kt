package po.exposify.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.OperationResult
import po.lognotify.eventhandler.models.Event



abstract class AbstractOperationResult : OperationResult {
    override fun isSuccess(): Boolean = true
}

data class CrudHostedResult<DATA, ENTITY>(
    val event: Event?
) : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity



data class CrudResult<DATA, ENTITY>(
    val event: Event? = null
) : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity