package po.exposify.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.OperationResult


abstract class AbstractOperationResult : OperationResult {
    override fun isSuccess(): Boolean = true
}

class CrudHostedResult<DATA, ENTITY>() : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity



class CrudResult<DATA, ENTITY>() : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity